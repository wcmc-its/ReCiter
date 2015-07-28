package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.GoldStandardPmidsDao;

public class GoldStandardPmidsDaoImpl implements GoldStandardPmidsDao {

	/**
	 * Retrieves the gold standard PMIDs for cwid.
	 * @param cwid
	 * @return set of PMIDs.
	 */
	@Override
	public List<String> getPmidsByCwid(String cwid) {
		List<String> pmids = new ArrayList<String>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT pmid from rc_gold_standard_pmids WHERE cwid = '" + cwid + "'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				pmids.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return pmids;
	}
}