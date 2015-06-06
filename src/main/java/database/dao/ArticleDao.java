package database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import database.DbConnectionFactory;
import database.DbUtil;

public class ArticleDao {

	/**
	 * Retrieves the gold standard PMIDs for cwid.
	 * @param cwid
	 * @return set of PMIDs.
	 */
	public Set<Integer> getPmidList(String cwid) {
		Set<Integer> pmidSet = new HashSet<Integer>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT rc_gold_standard_pmids.pmid from rc_gold_standard_pmids "
				+ "WHERE rc_gold_standard_pmids.cwid = '" + cwid + "'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				String pmidStr = rs.getString(1);
				try {
					if (pmidStr.contains("/")) {
						pmidStr = pmidStr.substring(0, pmidStr.indexOf("/"));
					}
					int pmid = Integer.parseInt(pmidStr);
					pmidSet.add(pmid);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return pmidSet;
	}
	
}