package database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DbConnectionFactory;
import database.DbUtil;

public class MatchingDepartmentsJournalsDao {

	public static void main(String[] args) {
		MatchingDepartmentsJournalsDao m = new MatchingDepartmentsJournalsDao();
		System.out.println(m.getScoreByJournalAndDepartment("Dan Med J", "Biochemistry"));
	}
	
	public double getScoreByJournalAndDepartment(String journal, String department) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = 
				"select score from wcmc_matching_departments_journals " +
						"where " +
						"journal_id in " +
						"(select id from wcmc_journals where journal = '" + journal + "') " +
						"and " + 
						"department_id in " +
						"(select id from wcmc_department_id where department = '" + department + "')";
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
