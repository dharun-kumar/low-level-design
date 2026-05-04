import java.util.ArrayList;
import java.util.Collections;

class Restaurant implements Comparable<Restaurant> {
    private String name;
    private double rating;
    private int distance;

    public Restaurant(String name, double rating, int distance) {
        this.name = name;
        this.rating = rating;
        this.distance = distance;
    }

    public int compareTo(Restaurant restaurant) {
        return this.distance - restaurant.distance;
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

public class ComparableDemo {

    public static void main(String[] args) {

        ArrayList<Restaurant> restaurants = new ArrayList<>();
        restaurants.add(new Restaurant("Thalapakatti", 4.0, 5));
        restaurants.add(new Restaurant("KFC", 4.1, 2));
        restaurants.add(new Restaurant("Mushroom Mama", 4.5, 7));

        // Sort restaurant using Comparable's compareTo method by distance
        Collections.sort(restaurants);

        for (Restaurant restaurant : restaurants) {
            System.out.println(restaurant.getName() + " " + restaurant.getRating() + " " + restaurant.getDistance());
        }

    }
}