import service.PubSubService;
import subscriber.SportsSubscriber;
import subscriber.Subscriber;
import subscriber.WeatherSubscriber;

public class PubSubDemo {

    public static void main(String[] args) throws InterruptedException {

        PubSubService service = PubSubService.getInstance();

        service.createTopic("sports");
        service.createTopic("weather");

        Subscriber sportsSubscriber = new SportsSubscriber();
        Subscriber weatherSubscriber = new WeatherSubscriber();

        service.subscribe("sports", sportsSubscriber);
        service.subscribe("weather", weatherSubscriber);

        service.publish("sports", "India won in world cup");
        service.publish("weather", "Red alert in chennai");

        Thread.sleep(1000);

        service.unSubscribe("sports", sportsSubscriber);
        service.subscribe("weather", sportsSubscriber);

        service.publish("weather", "Red alert in bangalore");

        Thread.sleep(1000);

        service.shutdown();

    }

}
