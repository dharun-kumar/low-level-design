import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


class Restaurant {
    private String name;
    private double rating;
    private int distance;

    public Restaurant(String name, double rating, int distance) {
        this.name = name;
        this.rating = rating;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public int getDistance() {
        return distance;
    }

}

// Comparator to sort Restaurants by rating (descending order)
class Rating implements Comparator<Restaurant> {

    public int compare(Restaurant restaurant1, Restaurant restaurant2) {
        return Double.compare(restaurant1.getRating(), restaurant2.getRating());
    }

}

// Comparator to sort Restaurants by name (alphabetical order)
class NameCompare implements Comparator<Restaurant> {

    public int compare(Restaurant restaurant1, Restaurant restaurant2) {
        return restaurant1.getName().compareTo(restaurant2.getName());
    }

}


public class ComparatorDemo {

    public static void main(String[] args) {

        ArrayList<Restaurant> restaurants = new ArrayList<>();
        restaurants.add(new Restaurant("Thalapakatti", 4.0, 5));
        restaurants.add(new Restaurant("KFC", 4.1, 2));
        restaurants.add(new Restaurant("Mushroom Mama", 4.5, 7));

        // Sort Restaurants by rating
        Collections.sort(restaurants, new Rating());


        for (Restaurant restaurant : restaurants) {
            System.out.println(restaurant.getName() + " " + restaurant.getRating() + " " + restaurant.getDistance());

        }

        // Sort Restaurants by name
        Collections.sort(restaurants, new NameCompare());


        for (Restaurant restaurant : restaurants) {
            System.out.println(restaurant.getName() + " " + restaurant.getRating() + " " + restaurant.getDistance());

        }
    }
}
