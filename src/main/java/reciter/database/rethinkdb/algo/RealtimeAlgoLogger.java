package reciter.database.rethinkdb.algo;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import reciter.database.rethinkdb.ChatController;

public class RealtimeAlgoLogger {
	
	private final static Logger slf4jLogger = LoggerFactory.getLogger(ChatController.class);

    private static final RethinkDB r = RethinkDB.r;

    private static RealtimeAlgoLogger realtimeAlgoLogger = new RealtimeAlgoLogger();
    
    private RealtimeAlgoLogger() {
    	Connection connection = RethinkDB.r.connection().hostname("localhost").port(28015).connect();
    	List<String> dbList = r.dbList().run(connection);
        if (!dbList.contains("algologs")) {
            r.dbCreate("algologs").run(connection);
        }
        List<String> tables = r.db("algologs").tableList().run(connection);
        if (!tables.contains("messages")) {
            r.db("algologs").tableCreate("messages").run(connection);
        }
    }

    public static RealtimeAlgoLogger getInstance(){
       return realtimeAlgoLogger;
    }

    public void insert(String message, LocalDateTime now){
       Connection conn = RethinkDB.r.connection().hostname("localhost").port(28015).connect();
       r.db("algologs").table("messages").insert(message).run(conn);
    }
    
    public void insert(String message){
//        Connection conn = RethinkDB.r.connection().hostname("localhost").port(28015).connect();
//        r.db("algologs").table("messages").insert(message).run(conn);
     }
}
