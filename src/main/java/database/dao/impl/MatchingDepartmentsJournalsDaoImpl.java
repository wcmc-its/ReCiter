package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.MatchingDepartmentsJournalsDao;

public class MatchingDepartmentsJournalsDaoImpl implements MatchingDepartmentsJournalsDao{

	/*public static void main(String[] args) {
		MatchingDepartmentsJournalsDaoImpl m = new MatchingDepartmentsJournalsDaoImpl();
		System.out.println(m.getScoreByJournalAndDepartment("Dan Med J", "Biochemistry"));
	}*/
	
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
	
	// @SuppressWarnings("null")
	public List<String> getDepartmentalAffiliationStringList() {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		List<String> departmentList = new ArrayList<String>(); 
		ResultSet rs = null;
		String query = "select primary_department, other_departments from rc_identity";
		String primaryDepartment = null;
		String otherDepartment = null;
		
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				primaryDepartment = rs.getString(1);
				if ( primaryDepartment != null && !primaryDepartment.trim().equals("") && !departmentList.contains(primaryDepartment)) {
					departmentList.add(primaryDepartment); 
				}
				otherDepartment = rs.getString(2);
				if ( otherDepartment != null && !otherDepartment.trim().equals("") && !departmentList.contains(otherDepartment)) {
					departmentList.add(otherDepartment); 
				}
				//departmentList.add(otherDepartment);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		if (departmentList != null ) {
			return departmentList;
		} else {
			return departmentList;
		}
			
	} 
	
	/**
	 * Translate the department names with / and & separators 
	 * Fix for issue # 79 Leverage departmental affiliation string matching for phase two matching
	 * @return List 
	 */
	
	//@SuppressWarnings("null")
	public List<String> getTranslatedDepartmentList() {
		List<String> translatedList = new ArrayList<String>();
		for ( String deptStr:  getDepartmentalAffiliationStringList()) {
			if ( deptStr.contains(" and ")) {
				translatedList.add(deptStr);
				translatedList.add(deptStr.replaceAll(" and ", " / "));
				translatedList.add(deptStr.replaceAll(" and ", "/"));
				translatedList.add(deptStr.replaceAll(" and ", " & "));
			} else {
				if ( deptStr != null ) {
						translatedList.add(deptStr);
				}
			}
		}
		//Set<String> uniqueVectorKeywords = new HashSet<String>(translatedList);
		//translatedList.addAll(uniqueVectorKeywords); 
		return translatedList; 
	}
}
