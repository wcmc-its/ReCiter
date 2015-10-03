package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.IdentityDegreeDao;
import database.model.IdentityDegree;


public class IdentityDegreeDaoImpl implements IdentityDegreeDao{
	
	/**
	 * Retrieves an IdentityDegree object to retrieve Identity Degree information from database.
	 * @param cwid
	 * @return
	 */
	public IdentityDegree getIdentityDegreeByCwid(String cwid) {
		
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT bachelor, masters, doctoral FROM rc_identity_degree WHERE cwid='" + cwid + "'";
		IdentityDegree identityDegree = new IdentityDegree();
		identityDegree.setCwid(cwid);
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				identityDegree.setBachelor(rs.getInt(1));
				identityDegree.setMasters(rs.getInt(2));
				identityDegree.setDoctoral(rs.getInt(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return identityDegree;
	}
}
