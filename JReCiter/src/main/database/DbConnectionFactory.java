package main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates a single connection to the database.
 * 
 * @author jil3004
 *
 */
public class DbConnectionFactory {
	
	private static DbConnectionFactory instance = new DbConnectionFactory();
	
	private static final String URL = "jdbc:mysql://localhost/reciter?rewriteBatchedStatements=true";
	private static final String USER = "root";
	private static final String PASSWORD = "";
	
	private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

	private DbConnectionFactory() {
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Connection createConnection() {
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
