interface BankFactory {
    Account createAccount();
    Card applyCard(Account Account);
}

interface Account {
    void deposit(double amount);
    double getBalance();
}

interface Card {
    void pay(double amount);
    double getCreditLimit();
}

class RetailFactory implements BankFactory {
    @Override
    public Account createAccount() {
        return new RetailAccount();
    }

    @Override
    public Card applyCard(Account account) {
        return new RetailCard(account);
    }
}

class RetailAccount implements Account {
    private double balance = 0;
    private String type = "Savings Account";

    @Override
    public void deposit(double amount) {
        balance += amount;
        System.out.println(type + " Balance: " + balance);
    }

    @Override
    public double getBalance() {
        return balance;
    }
}

class RetailCard implements Card {
    private Account account;
    private String type = "Debit Card";

    public RetailCard(Account account) {
        this.account = account;
    }

    @Override
    public void pay(double amount) {
        if(amount <= account.getBalance()) {
            System.out.print("Paid: " + amount + " | ");
            account.deposit(-amount);
        } else {
            System.out.println("Insufficient Account balance");
        }
    }

    @Override
    public double getCreditLimit() {
        return 0;
    }
}

class PremiumFactory implements BankFactory {
    @Override
    public Account createAccount() {
        return new PremiumAccount();
    }

    @Override
    public Card applyCard(Account account) {
        return new PremiumCard();
    }
}

class PremiumAccount implements Account {
    private double balance = 0;
    private String type = "Current Account";

    @Override
    public void deposit(double amount) {
        balance += amount;
        System.out.println(type + " Balance: " + balance);
    }

    @Override
    public double getBalance() {
        return balance;
    }
}

class PremiumCard implements Card {
    private double spent = 0;
    private static final double LIMIT = 25000;
    private String type = "Credit Card";

    @Override
    public void pay(double amount) {

        if(spent + amount <= LIMIT) {
            spent += amount;
            System.out.println("Paid: " + amount + " | Credit limit balance: " + (LIMIT - spent));
        } else {
            System.out.println("Insufficient Account balance");
        }
    }

    @Override
    public double getCreditLimit() {
        return LIMIT;
    }
}

public class AbstractFactoryDemo {
    public static void main(String[] args) {
        BankFactory retailFactory = new RetailFactory();
        Account retailAccount = retailFactory.createAccount();
        Card retailCard = retailFactory.applyCard(retailAccount);

        retailAccount.deposit(1000);
        retailCard.pay(499.87);

        BankFactory premiumFactory = new PremiumFactory();
        Account premiumAccount = premiumFactory.createAccount();
        Card premiumCard = premiumFactory.applyCard(premiumAccount);

        premiumAccount.deposit(2000);
        premiumCard.pay(13787.99);
    }
}