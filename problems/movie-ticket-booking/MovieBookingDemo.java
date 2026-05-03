import entities.Movie;
import enums.ShowTime;
import service.MovieBookingService;
import strategy.payment.CreditCardPayment;
import strategy.price.WeekEndPrice;
import view.ConsoleUI;
import java.util.Set;

public class MovieBookingDemo {

    public static void main(String[] args) {

        MovieBookingService service = MovieBookingService.getInstance();

        String dharun = service.createUser("dharun", "dharun@gmail.com");

        Movie interstellar = new Movie("Interstellar", 169);
        Movie darkKnight = new Movie("Dark Knight Rises", 164);
        Movie inception = new Movie("Inception", 148);

        String marinaMall = service.addTheater("Marina Mall", "Chennai");
        service.addScreen(marinaMall, "Screen 1", 10, 10);
        service.addScreen(marinaMall, "Screen 2", 10, 10);

        service.addShow(marinaMall, "Screen 1", interstellar, ShowTime.EVENING, new WeekEndPrice());
        service.addShow(marinaMall, "Screen 1", darkKnight, ShowTime.NIGHT, new WeekEndPrice());
        service.addShow(marinaMall, "Screen 2", inception, ShowTime.EVENING, new WeekEndPrice());

        String pvrCinema  = service.addTheater("PVR Cinemas", "Chennai");
        service.addScreen(pvrCinema, "Screen 5", 10, 10);
        service.addShow(pvrCinema, "Screen 5", interstellar, ShowTime.MORNING, new WeekEndPrice());

        // ------------------------------------------------------------------------------------------------------------

        service.searchTheatersByCity("Chennai")
                .forEach(theater -> System.out.println(theater.getName()));

        service.getMoviesByCity("Chennai")
                        .forEach(movie -> System.out.println(movie.getTitle()));

        service.searchShowsByTheater(marinaMall)
                .forEach(ConsoleUI::displayShow);

        service.searchTheatersByMovie(interstellar)
                .forEach(theater -> System.out.println(theater.getName()));

        service.getSeatAvailability(marinaMall, "Screen 1", interstellar, ShowTime.EVENING)
                .forEach(ConsoleUI::displaySeat);

        service.bookTicket(dharun, marinaMall, "Screen 1", interstellar, ShowTime.EVENING, Set.of("G1", "G2", "G3", "G4"), new CreditCardPayment())
                .thenAccept(ticket -> {
                    if(ticket != null) {
                        System.out.println(ticket.toString());
                    } else {
                        System.out.println("Failed to book ticket");
                    }
                    service.getSeatAvailability(marinaMall, "Screen 1", interstellar, ShowTime.EVENING)
                            .forEach(ConsoleUI::displaySeat);
                })
                .join();

        service.getTicketsByUser(dharun).forEach(System.out::println);

        service.shutdown();

    }

}
