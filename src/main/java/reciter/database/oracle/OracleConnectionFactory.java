package reciter.database.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OracleConnectionFactory {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(OracleConnectionFactory.class);

	@Value("${oracle.db.username}")
	private String username;

	@Value("${oracle.db.password}")
	private String password;

	@Value("${oracle.db.url}")
	private String url;

	public Connection createConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			slf4jLogger.error("Unable to establish connectino to oracle DB.", e);
		}
		return connection;
	}
}
