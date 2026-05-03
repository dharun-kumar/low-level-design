package entities;

import java.util.UUID;

public class Movie {

    private final UUID movieID;
    private final String title;
    private final int durationInMinutes;
    // todo cast, ratings etc ...

    public Movie(String title, int durationInMinutes) {
        this.movieID = UUID.randomUUID();
        this.title = title;
        this.durationInMinutes = durationInMinutes;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public String getMovieID() {
        return movieID.toString();
    }

}
