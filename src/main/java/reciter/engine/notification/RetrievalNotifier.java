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
	public void sendNotification(String cwid) {
		sendEmail(cwid);
	}

	private void sendEmail(String cwid) {
		SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("reciter.app.wcmc@gmail.com");
        message.setTo("reciter.app.wcmc@gmail.com");
        message.setSubject("Retrieval is completed.");
        message.setText("Retrieval has finished for " + cwid);
        mailSender.send(message);
	}

	@Override
	public void sendNotification() {
		// TODO Auto-generated method stub
		
	}
}
