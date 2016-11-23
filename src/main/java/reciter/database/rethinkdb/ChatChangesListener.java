package reciter.database.rethinkdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Cursor;

@Service
public class ChatChangesListener {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ChatChangesListener.class);

	private static final RethinkDB r = RethinkDB.r;

	@Autowired
	private RethinkDBConnectionFactory connectionFactory;

	@Autowired
	private SimpMessagingTemplate webSocket;

	@Async
	public void pushChangesToWebSocket() {
		Cursor<ChatMessage> cursor = r.db("chat").table("messages").changes()
				.getField("new_val")
				.run(connectionFactory.createConnection(), ChatMessage.class);

		while (cursor.hasNext()) {
			ChatMessage chatMessage = cursor.next();
			slf4jLogger.info("New message: {}", chatMessage);
			webSocket.convertAndSend("/topic/messages", chatMessage);
		}
	}
}