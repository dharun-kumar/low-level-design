package strategy.price;

import entities.Seat;
import java.util.Set;

public class WeekEndPrice implements PriceStrategy {

    private static final double weekEndSurgeFee = 1.2;

    @Override
    public double calculatePrice(Set<Seat> seats) {
        return seats.stream().mapToDouble(seat -> seat.getType().getPrice()).sum() * weekEndSurgeFee;
    }
}
