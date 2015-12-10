package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.IdentityAlternateDeptNamesDao;

public class IdentityAlternateDeptNamesDaoImpl implements IdentityAlternateDeptNamesDao {

	public static void main(String[] args) {
		IdentityAlternateDeptNamesDaoImpl d = new IdentityAlternateDeptNamesDaoImpl();
		List<String> names = d.getAlternateNames("Medicine");
		System.out.println(names);
	}

	@Override
	public List<String> getAlternateNames(String nameOfa) {
		List<String> alternateNames = new ArrayList<String>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT alternateName FROM rc_identity_alternate_dept_names WHERE nameOfa = '" + nameOfa + "'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				String result = rs.getString(1);
				if (result != null) {
					String[] alternateNamesArr = result.split("\\|");
					for (String alternateName : alternateNamesArr) {
						alternateNames.add(alternateName);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return alternateNames;
	}
}
