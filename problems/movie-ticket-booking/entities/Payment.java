package entities;

import enums.PaymentStatus;
import java.util.UUID;

public class Payment {

    private final UUID transactionID;
    private final double amount;
    private final PaymentStatus status;

    public Payment(double amount, PaymentStatus status) {
        this.transactionID = UUID.randomUUID();
        this.amount = amount;
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
