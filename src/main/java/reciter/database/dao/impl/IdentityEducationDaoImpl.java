package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityEducationDao;
import reciter.database.model.IdentityEducation;

/**
 * DAO class for table rc_identity_education.
 * @author jil3004
 *
 */
public class IdentityEducationDaoImpl implements IdentityEducationDao {

	public List<IdentityEducation> getEducation(String cwid) {
		List<IdentityEducation> list = new ArrayList<IdentityEducation>();
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		String query = 
				"select id, cwid, institution, degree_year, "
				+ "degree_field, inst_loc, inst_abbr "
				+ "from rc_identity_education where cwid = '" + cwid + "'";
			try {
				pst = con.createStatement();
				rs = pst.executeQuery(query);
				while (rs.next()) {
					IdentityEducation identityEducation = new IdentityEducation();
					identityEducation.setId(rs.getInt(1));
					identityEducation.setCwid(rs.getString(2));
					identityEducation.setInstitution(rs.getString(3));
					identityEducation.setDegreeYear(rs.getInt(4));
					identityEducation.setDegreeField(rs.getString(5));
					identityEducation.setInstLoc(rs.getString(6));
					identityEducation.setInstAbbr(rs.getString(7));
					list.add(identityEducation);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DbUtil.close(rs);
				DbUtil.close(pst);
				DbUtil.close(con);
			}
		return list;
	}
	
	public List<String> getIdentityCitizenshipEducation(String cwid) {
		List<String> returnList = new ArrayList<String>();
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		String query = "select institution from rc_identity_education where cwid = '" + cwid + "'";
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
