interface Strategy {
    void route(String source, String destination);;
}

public class Navigator {

    private Strategy strategy;

    public Navigator(Strategy strategy) {
        this.strategy = strategy;
    }

    public void changeStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void fetchDirections(String source, String destination) {
        strategy.route(source, destination);
    }
}

public class RoadStrategy implements Strategy {
    @Override
    public void route(String source, String destination) {
        System.out.println("Distance between " + source + " and " + destination + " is 350 Kms, It will take 6 hours 30 mins to reach there by Road.");
    }
}

public class AirStrategy implements Strategy{
    @Override
    public void route(String source, String destination) {
        System.out.println("Distance between " + source + " and " + destination + " is 350 Kms, It will take 1 hour to reach there by Air.");
    }
}

public class StrategyDemo {

    public static void main(String[] args) {
        Navigator maps = new Navigator(new RoadStrategy());
        maps.fetchDirections("Chennai", "Bangalore");
        
        maps.changeStrategy(new AirStrategy());
        maps.fetchDirections("Chennai", "Bangalore");
    }
}