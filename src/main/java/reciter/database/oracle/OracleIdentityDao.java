package reciter.database.oracle;

import java.util.List;

import reciter.model.identity.Grant;

public interface OracleIdentityDao {

	int getBachelorDegreeYear(String cwid);
	int getDoctoralYear(String cwid);
	List<String> getInstitutions(String cwid);
	List<String> getPersonalEmailFromOfa(String cwid);
	List<String> getGrants(String cwid);
	List<String> getRelationship(String cwid);
}
