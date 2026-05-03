package view;

import entities.Seat;
import entities.Show;

public class ConsoleUI {

    public static void displayShow(Show show) {
        String jsonTemplate = """
                    {
                        "Screen": "%s",
                        "Movie": "%s",
                        "Show Time": %s
                    }""";
        System.out.printf((jsonTemplate) + "%n", show.getScreen().getName(), show.getMovie().getTitle(), show.getShowTime());
    }

    public static void displaySeat(Seat seat) {
        String jsonTemplate = """ 
                {"Seat": "%s", "Type": "%s", "State": %s }""";
        System.out.printf((jsonTemplate) + "%n", seat.getSeatID(), seat.getType(), seat.getState());
    }

}
