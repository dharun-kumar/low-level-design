package observer;

import entities.Movie;
import entities.User;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MovieObserver {

    private final Set<User> subscribers;

    public MovieObserver() {
        this.subscribers = ConcurrentHashMap.newKeySet();
    }

    public void addSubscriber(User subscriber) {
        subscribers.add(subscriber);
    }

    public void removeSubscriber(User subscriber) {
        subscribers.remove(subscriber);
    }

    public void notifySubscribers(Movie movie) {
        subscribers.forEach(user -> user.notify(movie));
    }

}
