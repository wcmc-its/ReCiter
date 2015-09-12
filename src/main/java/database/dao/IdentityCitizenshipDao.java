package database.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;

/**
 * DAO class for table rc_identity_citizenship.
 * @author jil3004
 *
 */
public interface IdentityCitizenshipDao {

	// TODO: @Balu and @Hanumantha: Please create functions to get rows from the database table "rc_identity_citizenship".
	/* Function to get the identity citizen country from database table  
	 * @return country 
	 */
	public List<String> getIdentityCitizenshipCountry(String cwid);
}
