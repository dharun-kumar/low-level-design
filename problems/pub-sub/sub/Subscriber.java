package sub;

import message.Message;

public interface Subscriber {
    void onMessage(Message message);
}
