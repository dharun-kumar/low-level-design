package split;

import entity.Split;
import entity.User;
import java.util.List;
import java.util.Set;

public interface SplitStrategy {
    List<Split> getSplits(User paidBy, double totalAmount, List<User> participants, List<Double> splitAmounts);
}
