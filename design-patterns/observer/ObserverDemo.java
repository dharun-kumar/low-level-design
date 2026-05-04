import java.util.List;
import java.util.ArrayList;

interface Observer {
    void update(String message);
}

interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(String message);
}

class Investor implements Observer {
    private String name;

    public Investor(String name) {
        this.name = name;
    }

    @Override
    public void update(String stockPrice) {
        System.out.println(name + " stock prices updated -> " + stockPrice);
    }
}

class StockBroker implements Subject {
    private List<Observer> observers = new Arraylist<>();

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for(Observer observer : observers) {
            observer.update(message);
        }
    }

    public void updateStockPrice(String message) {
        notifyObservers(message);
    }
}

public class ObserverDemo {
    public static void main(String[] args) {

        StockBroker stockBroker = new StockBroker();
        Investor inverstor1 = new Investor("dharun");
        Investor inverstor2 = new Investor("kumar");

        stockBroker.addObserver(inverstor1);
        stockBroker.addObserver(inverstor2);
        stockBroker.updateStockPrice("Nitfy50 trading at 25333");

        stockBroker.removeObserver(inverstor2);
        stockBroker.updateStockPrice("Nitfy50 trading at 24876");
    }
}