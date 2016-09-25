package reciter.engine.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class RetrievalNotifier implements Notifier {

	@Autowired
    private JavaMailSender mailSender;
	
	@Override
	public void sendNotification() {
		sendEmail();
	}

	private void sendEmail() {
		SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("reciter.app.wcmc@gmail.com");
        message.setTo("reciter.app.wcmc@gmail.com");
        message.setSubject("hello");
        message.setText("Retrieval has finished.");
        mailSender.send(message);
	}
}
