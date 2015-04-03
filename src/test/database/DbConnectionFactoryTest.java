package test.database;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;

import main.database.DbConnectionFactory;

import org.junit.Test;

public class DbConnectionFactoryTest {

	/**
	 * Test Database connectivity.
	 */
	@Test
	public void testDatabaseConnectivity() {
		Connection con = DbConnectionFactory.getConnection();
		assertNotNull(con);
	}
}
