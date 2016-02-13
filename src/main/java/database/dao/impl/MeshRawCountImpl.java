package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.MeshRawCount;

public class MeshRawCountImpl implements MeshRawCount {
	
	@Override
	public long getCount(String mesh) {
		long count = 0;
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT count from wcmc_mesh_raw_count WHERE mesh = ?";
		try {
			pst = con.prepareStatement(query);
			pst.setString(1, mesh);
			rs = pst.executeQuery();
			while (rs.next()) {
				count = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return count;
	}
}
