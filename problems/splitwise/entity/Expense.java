package entity;

import split.SplitStrategy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Expense {

    private final UUID expenseID;
    private String description;
    private User paidBy;
    private double amount;
    private List<User> participants;
    private List<Split> splits;
    private final long timeStamp;


    public Expense(Builder builder) {
        this.expenseID = UUID.randomUUID();
        this.description = builder.description;
        this.paidBy = builder.paidBy;
        this.amount = builder.amount;
        this.participants = builder.participants;
        this.timeStamp = Instant.now().toEpochMilli();
        this.splits = builder.splitStrategy.getSplits(builder.paidBy, builder.amount, builder.participants, builder.splitValues);
    }

    public User getPaidBy() {
        return paidBy;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public List<Split> getSplits() {
        return splits;
    }

    public static class Builder {

        private String description;
        private User paidBy;
        private double amount;
        private List<User> participants;
        private List<Double> splitValues;
        private SplitStrategy splitStrategy;

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder paidBy(User paidBy) {
            this.paidBy = paidBy;
            return this;
        }

        public Builder participants(List<User> participants) {
            this.participants = participants;
            return this;
        }

        public Builder splitValues(List<Double> splitValues) {
            this.splitValues = splitValues;
            return this;
        }

        public Builder splitStrategy(SplitStrategy splitStrategy) {
            this.splitStrategy = splitStrategy;
            return this;
        }

        public Expense build() {
            return new Expense(this);
        }

    }

}
