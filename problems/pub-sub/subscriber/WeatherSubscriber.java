package subscriber;

import entities.Message;

public class WeatherSubscriber implements Subscriber {

    @Override
    public void onMessage(Message message) {
        System.out.println("Weather Subscriber :: " + message);
    }
}
