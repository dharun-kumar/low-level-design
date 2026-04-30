package entity;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Balance {

    private final User owner;
    private final Map<User, Double> balances;

    public Balance(User owner) {
        this.owner = owner;
        this.balances = new ConcurrentHashMap<>();
    }

    // user exists, adds amount to the current value; If not, sets the value to amount
    public void adjustBalance(User user, double amount) {
        if(!owner.equals(user)) {
            balances.merge(user, amount, Double::sum);
        }
    }

    public void displayBalance(Set<User> participants) {

        double totalAmountToPay = 0;
        double totalAmountToReceive = 0;

        for(Map.Entry<User, Double> balance : balances.entrySet()) {

            if(participants != null && !participants.contains(balance.getKey())) {
                continue;
            }

            if(balance.getValue() > 0.01) {
                System.out.printf("%s owes %s to %.2f%n", balance.getKey().getName(), owner.getName(), balance.getValue());
                totalAmountToReceive += balance.getValue();
            } else if(balance.getValue() < -0.01) {
                System.out.printf("%s owes %s to %.2f%n", owner.getName(), balance.getKey().getName(), Math.abs(balance.getValue()));
                totalAmountToPay += Math.abs(balance.getValue());
            }
        }

        System.out.println("Total amount to pay to other members :: " + totalAmountToPay);
        System.out.println("Total amount to receive by other members :: " + totalAmountToReceive + "\n");
    }

    public Map<User, Double> getAllBalances() {
        return Collections.unmodifiableMap(balances);
    }

}
