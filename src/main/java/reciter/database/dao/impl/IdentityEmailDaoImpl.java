package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityEmailDao;

@Repository("identityEmailDao")
public class IdentityEmailDaoImpl implements IdentityEmailDao {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(IdentityEmailDaoImpl.class);
	
	@Override
	public List<String> getEmailAddressesForCwid(String cwid) {
		List<String> emailAddresses = new ArrayList<String>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "select distinct email from rc_identity_email where cwid = ?;";
		try {
			pst = con.prepareStatement(query);
			pst.setString(1, cwid);
			rs = pst.executeQuery();
			while (rs.next()) {
				emailAddresses.add(rs.getString(1));
			}
		} catch (SQLException e) {
			slf4jLogger.error("Error retrieving email addresses for cwid=[" + cwid + "]", e);
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return emailAddresses;
	}
}
