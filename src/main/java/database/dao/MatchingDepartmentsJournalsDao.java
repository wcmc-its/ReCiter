package database.dao;

import java.util.List;
/**
 * 
 * @author htadimeti
 *
 */
public interface MatchingDepartmentsJournalsDao {	
	public double getScoreByJournalAndDepartment(String journal, String department);	
	public List<String> getDepartmentalAffiliationStringList();	
	public List<String> getTranslatedDepartmentList();
}
