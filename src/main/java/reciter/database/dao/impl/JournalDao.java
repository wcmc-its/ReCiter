package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;

public class JournalDao {

	static class JournalPair {
		String j1;
		String j2;

		public String getJ1() {
			return j1;
		}
		public void setJ1(String j1) {
			this.j1 = j1;
		}
		public String getJ2() {
			return j2;
		}
		public void setJ2(String j2) {
			this.j2 = j2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((j1 == null) ? 0 : j1.hashCode());
			result = prime * result + ((j2 == null) ? 0 : j2.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JournalPair other = (JournalPair) obj;
			if (j1 == null) {
				if (other.j1 != null)
					return false;
			} else if (!j1.equals(other.j1))
				return false;
			if (j2 == null) {
				if (other.j2 != null)
					return false;
			} else if (!j2.equals(other.j2))
				return false;
			return true;
		}

	}

	// A cache for journal similarity scores.
	private static Map<JournalPair, Double> simScoreCache = new HashMap<JournalPair, Double>();

	/**
	 * Retrieves the similarity score between journalA and journalB from the database.
	 * @param journalA
	 * @param journalB
	 * @return Non-negative score if score is found. Otherwise, returns -1.0.
	 */
	public double getJournalSimilarity(String journalA, String journalB) {
		JournalPair journalPair = new JournalPair();
		journalPair.setJ1(journalA);
		journalPair.setJ2(journalB);
		if (simScoreCache.containsKey(journalPair)) {
			return simScoreCache.get(journalPair);
		} else {
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
			simScoreCache.put(journalPair, score);
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
}
