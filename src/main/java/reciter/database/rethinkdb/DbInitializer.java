package reciter.database.rethinkdb;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

public class DbInitializer implements InitializingBean {
    @Autowired
    private RethinkDBConnectionFactory connectionFactory;

    @Autowired
    private ChatChangesListener chatChangesListener;

    private static final RethinkDB r = RethinkDB.r;

    @Override
    public void afterPropertiesSet() throws Exception {
        createDb();
        // we will see that later on
        chatChangesListener.pushChangesToWebSocket();
    }

    private void createDb() {
        Connection connection = connectionFactory.createConnection();
        List<String> dbList = r.dbList().run(connection);
        if (!dbList.contains("chat")) {
            r.dbCreate("chat").run(connection);
        }
        List<String> tables = r.db("chat").tableList().run(connection);
        if (!tables.contains("messages")) {
            r.db("chat").tableCreate("messages").run(connection);
            r.db("chat").table("messages").indexCreate("time").run(connection);
        }
    }
}