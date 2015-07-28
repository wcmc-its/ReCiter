package database.dao.impl;

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
public class IdentityCitizenshipDao {

	// TODO: @Balu and @Hanumantha: Please create functions to get rows from the database table "rc_identity_citizenship".
	/* Function to get the identity citizen country from database table  
	 * @return country 
	 */
	public List<String> getIdentityCitizenshipCountry(String cwid) {
		List<String> returnVal = new ArrayList<String>();
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		String query = "select country from rc_identity_citizenship where cwid = '" + cwid + "'";
		String country = null;
		try {
			pst = con.createStatement();
			rs = pst.executeQuery(query);
			while (rs.next()) {
				returnVal.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return returnVal;
	}
}
