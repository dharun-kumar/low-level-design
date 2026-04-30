package entity;

public class Transaction {

    private final User payer;
    private final User receiver;
    private final double amount;

    public Transaction(User payer, User receiver, double amount) {
        this.payer = payer;
        this.receiver = receiver;
        this.amount = amount;
    }

    public User getPayer() { return payer; }
    public User getReceiver() { return receiver; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("%s pays %s : %.2f", payer.getName(), receiver.getName(), amount);
    }
}