package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityDegreeDao;
import reciter.database.model.IdentityDegree;

@Repository("identityDegreeDao")
public class IdentityDegreeDaoImpl implements IdentityDegreeDao {
	
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
	
	public Map<String, IdentityDegree> getAllIdentityDegree() {
		Map<String, IdentityDegree> m = new HashMap<String, IdentityDegree>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT cwid, bachelor, masters, doctoral FROM rc_identity_degree where cwid is not null and cwid not regexp '^[0-9]+'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				String cwid = rs.getString(1);
				IdentityDegree identityDegree = new IdentityDegree();
				identityDegree.setBachelor(rs.getInt(2));
				identityDegree.setMasters(rs.getInt(3));
				identityDegree.setDoctoral(rs.getInt(4));
				m.put(cwid, identityDegree);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return m;
	}
}
