package reciter.database.rethinkdb;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.rethinkdb.RethinkDB;

@RestController
@RequestMapping("/chat")
public class ChatController {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ChatController.class);

    private static final RethinkDB r = RethinkDB.r;

    @Autowired
    private RethinkDBConnectionFactory connectionFactory;

    @RequestMapping(method = RequestMethod.POST)
    public ChatMessage postMessage(@RequestBody ChatMessage chatMessage) {
        chatMessage.setTime(OffsetDateTime.now());
        Object run = r.db("chat").table("messages").insert(chatMessage)
                .run(connectionFactory.createConnection());

        slf4jLogger.info("Insert {}", run);
        return chatMessage;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ChatMessage> getMessages() {

        List<ChatMessage> messages = r.db("chat").table("messages")
                .orderBy().optArg("index", r.desc("time"))
                .limit(20)
                .orderBy("time")
                .run(connectionFactory.createConnection(), ChatMessage.class);

        return messages;
    }
}