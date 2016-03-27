package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityEarliestStartDateDao;
import reciter.database.model.IdentityEarliestStartDate;

public class IdentityEarliestStartDateDaoImpl implements IdentityEarliestStartDateDao{

	@Override
	public List<IdentityEarliestStartDate> getAllIdentityEarliestStartDates() {
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

	@Override
	public IdentityEarliestStartDate getIdentityEarliestStartDateByCwid(String cwid) {
		IdentityEarliestStartDate date=null;
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT id,cwid,startdate from rc_identity_earlieststartdate where cwid='"+cwid+"'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				 date = new IdentityEarliestStartDate();
				date.setId(rs.getInt(1));
				date.setCwid(rs.getString(2));
				date.setStartDate(rs.getString(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return date;
	}

}
