package strategy;

import entity.Split;
import entity.User;

import java.util.ArrayList;
import java.util.List;

public class PercentSplit implements SplitStrategy {
    @Override
    public List<Split> getSplits(User paidBy, double totalAmount, List<User> participants, List<Double> splitAmounts) {

        if(participants.size() != splitAmounts.size()) {
            throw new IllegalArgumentException("participants and split count must match");
        }

        if(Math.abs(splitAmounts.stream().mapToDouble(Double::doubleValue).sum() - 100.0) > 0.01) {
            throw new IllegalArgumentException("sum of split percentages must be 100");
        }

        List<Split> splits = new ArrayList<>();
        for(int i=0; i<participants.size() ; i++) {
            splits.add(new Split(participants.get(i), totalAmount * (splitAmounts.get(i) / 100.0)));
        }
        return splits;
    }
}
