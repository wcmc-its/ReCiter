package reciter.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.xml.parser.AbstractXmlFetcher;

/**
 * DbUtil for closing MySQL database connection.
 * 
 * @author jil3004
 *
 */
public class DbUtil {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(AbstractXmlFetcher.class);

	public static void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				slf4jLogger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				slf4jLogger.error(e.getMessage(), e);
			}
		}
	}

	public static void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				slf4jLogger.error(e.getMessage(), e);
			}
		}
	}
}
