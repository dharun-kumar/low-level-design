package strategy;

import entity.Split;
import entity.User;
import java.util.List;

public interface SplitStrategy {
    List<Split> getSplits(User paidBy, double totalAmount, List<User> participants, List<Double> splitAmounts);
}
