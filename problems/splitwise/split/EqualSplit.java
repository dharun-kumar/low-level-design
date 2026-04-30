package split;

import entity.Split;
import entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EqualSplit implements SplitStrategy {
    @Override
    public List<Split> getSplits(User paidBy, double totalAmount, List<User> participants, List<Double> splitAmounts) {
        double splitAmount = totalAmount / participants.size();
        List<Split> splits = new ArrayList<>();
        for(User participant : participants) {
            splits.add(new Split(participant, splitAmount));
        }
        return splits;
    }
}