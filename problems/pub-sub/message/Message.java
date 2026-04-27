package message;

import java.time.Instant;

public class Message {

    private final String payload;
    private final long timeStamp;

    public Message(String payload) {
        this.payload = payload;
        this.timeStamp = Instant.now().toEpochMilli();
    }

    public String getPayload() {
        return payload;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "{\n\tPayload : " + getPayload() + " ,\n\t" + "Timestamp : " + getTimeStamp() + "\n}";
    }

}
