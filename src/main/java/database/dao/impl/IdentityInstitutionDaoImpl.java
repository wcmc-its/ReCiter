package database.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.IdentityInstitutionDao;

public class IdentityInstitutionDaoImpl implements IdentityInstitutionDao {
	
	@Override
	public List<String> getInstitutionByCwid(String cwid) {
		Connection con = DbConnectionFactory.getConnection();
		List<String> institutions = new ArrayList<String>();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("select institution from rc_identity_institution where cwid='" + cwid + "'");
			
			while (rs.next()) {
				institutions.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(st);
			DbUtil.close(con);
		}
		return institutions;
	}
}
