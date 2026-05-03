package strategy.payment;

import entities.Payment;
import enums.PaymentStatus;

public class CreditCardPayment implements PaymentStrategy {

    @Override
    public Payment pay(double amount) {
        try {
            Thread.sleep(5000); // Mimic delay
            return new Payment(amount, Math.random() > 0.5 ? PaymentStatus.SUCCESS : PaymentStatus.FAILURE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Payment(amount, PaymentStatus.FAILURE);
        }
    }
}
