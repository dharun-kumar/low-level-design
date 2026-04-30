package service;

import entities.Message;
import subscriber.Subscriber;
import entities.Topic;
import java.util.Map;
import java.util.concurrent.*;

public class PubSubService {

    private static volatile PubSubService INSTANCE;
    private final Map<String,Topic> topics;
    private final ExecutorService executorService;

    private PubSubService() {
        topics = new ConcurrentHashMap<>();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static PubSubService getInstance() {
        if(INSTANCE == null) {
            synchronized(PubSubService.class) {
                if(INSTANCE == null) {
                    INSTANCE = new PubSubService();
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void createTopic(String topicName) {
        topics.putIfAbsent(topicName, new Topic(topicName));
    }

    public synchronized void publish(String topicName, String payload) {
        topics.computeIfPresent(topicName, (name, topic) -> {
            topic.addMessage(new Message(payload));
            executorService.submit(topic::broadCastMessages);
            return topic;
        });
    }

    public synchronized void subscribe(String topicName, Subscriber subscriber) {
        topics.computeIfPresent(topicName, (name, topic) -> {
            topic.addSubscriber(subscriber);
            return topic;
        });
    }

    public synchronized void unSubscribe(String topicName, Subscriber subscriber) {
        topics.computeIfPresent(topicName, (name, topic) -> {
            topic.removeSubscriber(subscriber);
            return topic;
        });
    }

    public void shutdown() {

        for (Topic topic : topics.values()) {
            topic.broadCastMessages();
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
