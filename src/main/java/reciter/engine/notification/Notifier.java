package reciter.engine.notification;

public interface Notifier {
	void sendNotification();

	void sendNotification(String cwid);
}
