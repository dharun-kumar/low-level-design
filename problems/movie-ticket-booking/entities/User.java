package entities;

import java.util.UUID;

public class User {

    private final UUID userID;
    private final String name;
    private final String email;

    public User(String name, String email) {
        this.userID = UUID.randomUUID();
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUserID() {
        return userID.toString();
    }

    public void notify(Movie movie) {
        System.out.printf("Hi %s (%s): Booking opens for the movie '%s'!%n", name, email, movie.getTitle());
    }

}
