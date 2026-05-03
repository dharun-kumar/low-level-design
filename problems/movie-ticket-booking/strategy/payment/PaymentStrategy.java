package strategy.payment;

import entities.Payment;

public interface PaymentStrategy {
    Payment pay(double amount);
}
