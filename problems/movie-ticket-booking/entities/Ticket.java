package entities;

import java.util.Set;
import java.util.UUID;

public class Ticket {

    private final UUID ticketID;
    private final User user;
    private final Show show;
    private final Set<Seat> seats;
    private final Payment payment;

    public Ticket(User user, Show show, Set<Seat> seats, Payment payment) {
        this.ticketID = UUID.randomUUID();
        this.user = user;
        this.show = show;
        this.seats = seats;
        this.payment = payment;
    }

    public String getTicketID() {
        return ticketID.toString();
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        String jsonTemplate = """
                    {
                        "Ticket ID": "%s",
                        "User Email": "%s",
                        "Theater": "%s",
                        "Screen": "%s",
                        "Movie": "%s",
                        "Show Time": "%s",
                        "Seats": "%s",
                        "price": "%.2f"
                    }""";
        return String.format(jsonTemplate + "%n", ticketID, user.getEmail(), show.getTheater().getName(), show.getScreen().getName(), show.getMovie().getTitle(), show.getShowTime() + " " + show.getShowTime().getTime(), seats.toString(), payment.getAmount());
    }

}
