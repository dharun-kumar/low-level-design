package sub;

import message.Message;

public class WeatherSubscriber implements Subscriber {

    @Override
    public void onMessage(Message message) {
        System.out.println("Weather Subscriber :: " + message);
    }
}
