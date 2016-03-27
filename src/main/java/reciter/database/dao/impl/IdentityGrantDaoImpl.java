package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityGrantDao;
import reciter.database.model.IdentityGrant;

public class IdentityGrantDaoImpl implements IdentityGrantDao{

	/**
	 * Returns a list of IdentityGrant objects by query the cwid in table rc_identity_grant.
	 * @param cwid cwid.
	 * @return List of IdentityGrants.
	 */
	@Override
	public List<IdentityGrant> getIdentityGrantListByCwid(String cwid) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT id, cwid, date, grantid, sponsorAwardId, "
				+ "administeringDepartmentDivision, awardingOrganization FROM rc_identity_grant "
				+ "WHERE cwid='" + cwid + "'";
		List<IdentityGrant> list = new ArrayList<IdentityGrant>();
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				IdentityGrant identityGrant = new IdentityGrant();
				identityGrant.setId(rs.getLong(1));
				identityGrant.setCwid(rs.getString(2));
				identityGrant.setDate(rs.getString(3));
				identityGrant.setGrandid(rs.getString(4));
				identityGrant.setSponsorAwardId(rs.getString(5));
				identityGrant.setAdministeringDepartmentDivision(rs.getString(6));
				identityGrant.setAwardingOrganization(rs.getString(7));
				list.add(identityGrant);
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
	
	@Override
	public List<String> getSponsorAwardIdListByCwid(String cwid) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT sponsorAwardId FROM rc_identity_grant WHERE cwid='" + cwid + "'";
		List<String> list = new ArrayList<String>();
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				list.add(rs.getString(1));
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
}
