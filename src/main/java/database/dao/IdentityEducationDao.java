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
 * DAO class for table rc_identity_education.
 * @author jil3004
 *
 */
public class IdentityEducationDao {

	// TODO: @Balu and @Hanumantha: Please create functions to get rows from the database table "rc_identity_education".
	public List<String> getIdentityCitizenshipEducation(String cwid) {
		List<String> returnList = new ArrayList<String>();
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		String query = null;
			query = "select institution from rc_identity_education where cwid = '" + cwid + "'";
			try {
				pst = con.createStatement();
				rs = pst.executeQuery(query);
				while (rs.next()) {
					returnList.add(rs.getString(1));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtil.close(rs);
				DbUtil.close(pst);
				DbUtil.close(con);
			}
			return returnList;
	}
}
