package reciter.database.rethinkdb;

import java.time.OffsetDateTime;

public class ChatMessage {
    public String message;
    public String from;
    public OffsetDateTime time;

    public ChatMessage() {
    }

    @Override
	public String toString() {
		return "ChatMessage [message=" + message + ", from=" + from + ", time=" + time + "]";
	}

	public ChatMessage(String message, String from, OffsetDateTime time) {
        this.message = message;
        this.from = from;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }
}