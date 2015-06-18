package database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DbConnectionFactory;
import database.DbUtil;
import database.model.Identity;

public class IdentityDao {

	/**
	 * Retrieves identity information for cwid. Identity information includes first name, last name, middle name, etc.
	 * @param cwid
	 * @return Identity object for cwid.
	 */
	public Identity getIdentityByCwid(String cwid) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT first_name, last_name, middle_name, title, primary_department, primary_affiliation FROM rc_identity WHERE cwid='" + cwid + "'";
		Identity identity = new Identity();
		identity.setCwid(cwid);
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				identity.setFirstName(rs.getString(1));
				identity.setLastName(rs.getString(2));
				identity.setMiddleName(rs.getString(3));
				identity.setTitle(rs.getString(4));
				identity.setPrimaryDepartment(rs.getString(5));
				identity.setPrimaryAffiliation(rs.getString(6));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return identity;
	}
}
