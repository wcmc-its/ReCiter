package reciter.database.oracle;

import java.util.List;

public interface OracleIdentityDao {

	int getBachelorDegreeYear(String uid);
	int getDoctoralYear(String uid);
	List<String> getInstitutions(String uid);
	List<String> getPersonalEmailFromOfa(String uid);
	List<String> getGrants(String uid);
	List<String> getRelationshipCwids(String uid);
}
