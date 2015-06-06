package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates a single connection to the database.
 * 
 * @author jil3004
 *
 */
public class DbConnectionFactory {
	
	private static DbConnectionFactory instance = new DbConnectionFactory();
	
	private static String URL;
	private static String USER;
	private static String PASSWORD;
	
	public void loadProperty() throws IOException {
		Properties prop = new Properties();
		InputStream inputStream = new FileInputStream("configs/database.properties");
		prop.load(inputStream);
		
		URL = prop.getProperty("url");
		USER = prop.getProperty("username");
		PASSWORD = prop.getProperty("password");
	}
	
	private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

	private DbConnectionFactory() {
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Connection createConnection() {
		try {
			loadProperty();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	public static Connection getConnection() {
		return instance.createConnection();
	}
}
