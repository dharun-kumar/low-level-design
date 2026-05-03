package entities;

import enums.SeatType;
import enums.ShowTime;
import strategy.price.PriceStrategy;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Show {

    private final Theater theater;
    private final String showID;
    private final Screen screen;
    private final Movie movie;
    private final ShowTime showTime;
    private final Map<String, Seat> seats;
    private final PriceStrategy priceStrategy;

    public Show(Theater theater, Screen screen, Movie movie, ShowTime showTime, PriceStrategy priceStrategy) {
        this.theater = theater;
        this.showID = theater.getTheaterID() + screen.getName() + movie.getMovieID() + showTime;
        this.screen = screen;
        this.movie = movie;
        this.showTime = showTime;
        this.seats = new ConcurrentHashMap<>();
        this.priceStrategy = priceStrategy;

        int zoneDelimiter = screen.getRows()/2;
        for(int row=0; row<screen.getRows(); row++) {
            for(int col=1; col<=screen.getSeatsPerRow(); col++) {
                String cell = String.valueOf((char) ('A' + row)) + col;
                seats.putIfAbsent(cell, new Seat(cell, row < zoneDelimiter ? SeatType.REGULAR : SeatType.PREMIUM));
            }
        }
    }

    public Seat getSeat(String seatID) {
        return seats.get(seatID);
    }

    public Collection<Seat> getSeats() {
        return seats.values();
    }

    public String getShowID() {
        return showID;
    }

    public Screen getScreen() {
        return screen;
    }

    public ShowTime getShowTime() {
        return showTime;
    }

    public Movie getMovie() {
        return movie;
    }

    public Theater getTheater() {
        return theater;
    }

    public PriceStrategy getPriceStrategy() {
        return priceStrategy;
    }
}
