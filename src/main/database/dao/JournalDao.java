package main.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.database.DbConnectionFactory;
import main.database.DbUtil;

public class JournalDao {
	
	/**
	 * Retrieves the similarity score between journalA and journalB from the database.
	 * @param journalA
	 * @param journalB
	 * @return Non-negative score if score is found. Otherwise, returns -1.0.
	 */
	public double getJournalSimilarity(String journalA, String journalB) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = 
				"select score from wcmc_matching_journals_journals_compact " +
				"where " +
				"journal1 in " +
				"(select id from wcmc_journals where journal = '" + journalA + "') " +
				"and " + 
				"journal2 in " +
				"(select id from wcmc_journals where journal = '" + journalB + "')";
		double score = -1;
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				score = rs.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return score;
	}
}
