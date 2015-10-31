package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.IdentityEarliestStartDateDao;
import database.model.IdentityEarliestStartDate;

public class IdentityEarliestStartDateDaoImpl implements IdentityEarliestStartDateDao{

	@Override
	public List<IdentityEarliestStartDate> getAllDaos() {
		List<IdentityEarliestStartDate> list = new ArrayList<IdentityEarliestStartDate>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT id,cwid,startdate from rc_identity_earlieststartdate";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				IdentityEarliestStartDate date = new IdentityEarliestStartDate();
				date.setId(rs.getInt(1));
				date.setCwid(rs.getString(2));
				date.setStartDate(rs.getString(3));
				list.add(date);
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
