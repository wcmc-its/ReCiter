package database.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.BoardCertificationDao;
/**
 * 
 * @author htadimeti
 *
 */
public class BoardCertificationDaoImpl implements BoardCertificationDao {
	/**
	 * 
	 * @param cwid
	 * @return
	 * @throws SQLException
	 */
	public List<String> getBoardCertificationsByCwid(String cwid) {
		Connection con = DbConnectionFactory.getConnection();
		List<String> boardCertifications = new ArrayList<String>();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("select specialty from rc_identity_board_certifications where cwid='" + cwid + "'");
			
			while (rs.next()) {
				boardCertifications.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(st);
			DbUtil.close(con);
		}
		return boardCertifications;
	}
}
