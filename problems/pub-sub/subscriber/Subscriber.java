package subscriber;

import entities.Message;

public interface Subscriber {
    void onMessage(Message message);
}
