package subscriber;

import entities.Message;

public class SportsSubscriber implements Subscriber {

    @Override
    public void onMessage(Message message) {
        System.out.println("Sports Subscriber :: " + message.toString());
    }
}
