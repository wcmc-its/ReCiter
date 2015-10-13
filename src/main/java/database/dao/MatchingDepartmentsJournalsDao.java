package database.dao;

import java.util.List;
/**
 * 
 * @author htadimeti
 *
 */
public interface MatchingDepartmentsJournalsDao {	
	double getScoreByJournalAndDepartment(String journal, String department);	
	List<String> getDepartmentalAffiliationStringList();	
	List<String> getTranslatedDepartmentList();
}
