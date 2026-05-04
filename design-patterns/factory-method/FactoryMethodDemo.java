import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

interface Product {
    Payment pay(double amount);
}

interface Creator {
    Product createProcessor();
}

class PayPal implements Product {
    private final List<Payment> transactions = new ArrayList<>();

    @Override
    public Payment pay(double amount) {
        System.out.println("Processing payment via PayPal...");
        Payment payment = new Payment(amount, "SUCCESS", "PayPal");
        transactions.add(payment);
        return payment;
    }
}

class Stripe implements Product {
    private final List<Payment> transactions = new ArrayList<>();

    @Override
    public Payment pay(double amount) {
        System.out.println("Processing payment via Stripe...");
        Payment payment = new Payment(amount, "SUCCESS", "Stripe");
        transactions.add(payment);
        return payment;
    }
}

class PayPalProcessor implements Creator {
    @Override
    public Product createProcessor() {
        return new PayPal();
    }
}

class StripeProcessor implements Creator{
    @Override
    public Product createProcessor() {
        return new Stripe();
    }
}

class Payment {
    private final UUID transactionID;
    private final double amount;
    private final String status;
    private final String provider;

    public Payment(double amount, String status, String provider) {
        this.transactionID = UUID.randomUUID();
        this.amount = amount;
        this.status = status;
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "Payment : { transactionID : " + transactionID + ", amount : " + amount + ", status : " + status + ", provider : " + provider + " }";
    }
}

public class FactoryMethodDemo {
    public static void main(String[] args) {
        Creator payPalProcessor = new PayPalProcessor();
        Product paypal = payPalProcessor.createProcessor();
        System.out.println(paypal.pay(417.99).toString());

        Creator stripeProcessor = new StripeProcessor();
        Product stripe = stripeProcessor.createProcessor();
        System.out.println(stripe.pay(513.49).toString());
    }
}
