package entities;

import enums.ShowTime;
import strategy.price.PriceStrategy;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Theater {

    private final UUID theaterID;
    private final String name;
    private final City city;
    private final Map<String, Screen> screens;
    private final Map<String, Show> shows;

    public Theater(String name, City city) {
        this.theaterID = UUID.randomUUID();
        this.name = name;
        this.city = city;
        this.screens = new ConcurrentHashMap<>();
        this.shows = new ConcurrentHashMap<>();
    }

    public void addScreen(Screen screen) {
        screens.putIfAbsent(screen.getName(), screen);
    }

    public void addShow(String screenName, Movie movie, ShowTime showTime, PriceStrategy priceStrategy) {
        Show show = new Show(this, screens.get(screenName), movie, showTime, priceStrategy);
        shows.putIfAbsent(show.getShowID(), show);
    }

    public Collection<Show> getShows() {
        return shows.values();
    }

    public Show getShow(String screenName, String movieID, ShowTime showTime) {
        return shows.get(theaterID + screenName + movieID + showTime);
    }

    public String getTheaterID() {
        return theaterID.toString();
    }

    public City getCity() {
        return city;
    }

    public String getName() {
        return name;
    }
}
