package entity;

public class Split {

    private final User participant;
    private final double amount;

    public Split(User participant, double amount) {
        this.participant = participant;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public User getParticipant() {
        return participant;
    }
}
