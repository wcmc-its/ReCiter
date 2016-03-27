package reciter.database.dao;

import java.util.List;

public interface IdentityInstitutionDao {

	List<String> getInstitutionByCwid(String cwid);
}
