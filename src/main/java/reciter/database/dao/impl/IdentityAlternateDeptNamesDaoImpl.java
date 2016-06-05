package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityAlternateDeptNamesDao;

@Repository("identityAlternateDeptNamesDao")
public class IdentityAlternateDeptNamesDaoImpl implements IdentityAlternateDeptNamesDao {

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
