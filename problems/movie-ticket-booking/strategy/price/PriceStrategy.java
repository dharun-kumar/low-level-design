package strategy.price;

import entities.Seat;
import java.util.Set;

public interface PriceStrategy {
    double calculatePrice(Set<Seat> seats);
}
