package reciter.database.oracle;

import java.util.List;

public interface OracleIdentityDao {

	int getBachelorDegreeYear(String cwid);
	int getDoctoralYear(String cwid);
	List<String> getInstitutions(String cwid);
	List<String> getPersonalEmailFromOfa(String cwid);
}
