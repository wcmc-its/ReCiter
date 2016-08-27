package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.MeshRawCount;
import reciter.database.mongo.model.MeshTerm;

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
	
	@Override
	public List<MeshTerm> getAllMeshTerms() {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<MeshTerm> meshTerms = new ArrayList<MeshTerm>();
		String query = "SELECT mesh, count from wcmc_mesh_raw_count where mesh is not null";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				MeshTerm meshTerm = new MeshTerm();
				meshTerm.setMesh(rs.getString(1));
				meshTerm.setCount(rs.getLong(2));
				meshTerms.add(meshTerm);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return meshTerms;
	}
}
