package topic;

import message.Message;
import sub.Subscriber;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

public class Topic {

    private final String topicName;
    private final Queue<Message> messages;
    private final Set<Subscriber> subscribers;

    public Topic(String topicName) {
        this.topicName = topicName;
        this.messages = new ConcurrentLinkedQueue<>();
        this.subscribers = new CopyOnWriteArraySet<>();
    }

    public void addSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void broadCastMessages() {
        while(!messages.isEmpty()) {
            Message message = messages.poll();
            for(Subscriber subscriber : subscribers) {
                subscriber.onMessage(message);
            }
        }
    }

}