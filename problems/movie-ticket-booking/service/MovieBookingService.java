package service;

import entities.*;
import enums.ShowTime;
import observer.MovieObserver;
import strategy.payment.PaymentStrategy;
import strategy.price.PriceStrategy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MovieBookingService {

    private static MovieBookingService INSTANCE;
    private final Map<String, Theater> theaters;
    private final Map<String, City> cities;
    private final Map<City, Set<Movie>> movies;
    private final Map<String, User> users;
    private final Map<User, Set<Ticket>> tickets;
    private final BookingManager bookingManager;
    private final MovieObserver observer;

    private MovieBookingService() {
        this.theaters = new ConcurrentHashMap<>();
        this.cities = new ConcurrentHashMap<>();
        this.movies = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.tickets = new ConcurrentHashMap<>();
        this.bookingManager = new BookingManager();
        this.observer = new MovieObserver();
    }

    public synchronized static MovieBookingService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MovieBookingService();
        }
        return INSTANCE;
    }

    public synchronized String addTheater(String theaterName, String cityName) {
        cities.putIfAbsent(cityName, new City(cityName));
        Theater theater = new Theater(theaterName, cities.get(cityName));
        theaters.putIfAbsent(theater.getTheaterID(), theater);
        return theater.getTheaterID();
    }

    public synchronized void addScreen(String theaterID, String screenName, int rows, int seatsPerRow) {
        theaters.get(theaterID).addScreen(new Screen(screenName, rows, seatsPerRow));
    }

    public synchronized void addShow(String theaterID, String screenName, Movie movie, ShowTime showTime, PriceStrategy priceStrategy) {
        movies.computeIfAbsent(theaters.get(theaterID).getCity(), value -> new HashSet<>())
                        .add(movie);
        theaters.get(theaterID).addShow(screenName , movie, showTime, priceStrategy);
        observer.notifySubscribers(movie);  //todo fix : notify for each show
    }

    public synchronized String createUser(String name, String email) {
        User user = new User(name, email);
        users.putIfAbsent(user.getUserID(), user);
        observer.addSubscriber(user);
        return user.getUserID();
    }

    //Fetch all theaters in the city
    public synchronized List<Theater> searchTheatersByCity(String name) {
        return theaters.values().stream()
                .filter((theater -> theater.getCity().getName().equalsIgnoreCase(name)))
                .toList(); //todo O(n), can be reduced to O(1) using map
    }

    //Fetch movies streaming in the city
    public synchronized Collection<Movie> getMoviesByCity(String cityName) {
        return movies.get(cities.get(cityName));
    }

    //Fetch all shows available in the theater
    public synchronized Collection<Show> searchShowsByTheater(String theaterID) {
        return theaters.get(theaterID).getShows();
        //todo only list shows which is greater than current time
    }

    //Fetch all theaters which screens the movies
    public synchronized Collection<Theater> searchTheatersByMovie(Movie movie) {
        return theaters.values().stream()
                .filter(theater -> theater.getShows().stream()
                        .anyMatch(show -> show.getMovie().equals(movie)))
                .toList();
    }

    //Get all seats & its state for request show details
    public synchronized Collection<Seat> getSeatAvailability(String theaterID, String screenName, Movie movie, ShowTime showTime) {
        Show show = theaters.get(theaterID).getShow(screenName, movie.getMovieID(), showTime);
        return show.getSeats();
    }

    public synchronized CompletableFuture<Ticket> bookTicket(String userID, String theaterID, String screenName, Movie movie, ShowTime showTime, Set<String> seatIDs, PaymentStrategy paymentStrategy) {
        Show show = theaters.get(theaterID).getShow(screenName, movie.getMovieID(), showTime);
        Set<Seat> seats = seatIDs.stream()
                .map(show::getSeat)
                .collect(Collectors.toSet());
        return bookingManager.processBooking(users.get(userID), show, seats, paymentStrategy)
                .thenApply(ticket -> {
                    if (ticket != null) {
                        tickets.computeIfAbsent(users.get(userID), value -> ConcurrentHashMap.newKeySet())
                                        .add(ticket);
                    }
                    return ticket;
                });
    }

    public synchronized Set<Ticket> getTicketsByUser(String userID) {
        return tickets.getOrDefault(users.get(userID), Collections.emptySet());
    }

    public synchronized void shutdown() {
        bookingManager.shutdown();
    }

}