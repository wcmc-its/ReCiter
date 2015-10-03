package database.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.IdentityCitizenshipDao;

public class IdentityCitizenshipDaoImpl implements IdentityCitizenshipDao {

	public String getIdentityCitizenshipCountry(String cwid) {
		String citizenship = null;
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		String query = "select country from rc_identity_citizenship where cwid = '" + cwid + "'";
		try {
			pst = con.createStatement();
			rs = pst.executeQuery(query);
			while (rs.next()) {
				citizenship = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return citizenship;
	}
}
