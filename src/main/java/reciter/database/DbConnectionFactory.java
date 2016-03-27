package reciter.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a single connection to the database.
 * 
 * @author jil3004
 *
 */
public class DbConnectionFactory {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(DbConnectionFactory.class);

	private static DbConnectionFactory instance = new DbConnectionFactory();
	private static final String DB_CONFIG = "src/main/resources/config/database.properties";
	private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

	private String url;
	private String userName;
	private String passWord;
	
	private void loadProperty() throws IOException {
		Properties p = new Properties();
		InputStream inputStream = new FileInputStream(DB_CONFIG);
		p.load(inputStream);
		
		url = p.getProperty("url");
		userName = p.getProperty("username");
		passWord = p.getProperty("password");
	}
	

	private DbConnectionFactory() {
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			slf4jLogger.error(e.getMessage(), e);
		}
	}

	private Connection createConnection() {
		try {
			loadProperty();
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage(), e);
		}
		
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, userName, passWord);
		} catch (SQLException e) {
			slf4jLogger.error(e.getMessage(), e);
		}
		return connection;
	}

	public static Connection getConnection() {
		return instance.createConnection();
	}
}
