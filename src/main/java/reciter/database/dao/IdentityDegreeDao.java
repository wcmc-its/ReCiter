package reciter.database.dao;

import reciter.database.model.IdentityDegree;

public interface IdentityDegreeDao {
	
	/**
	 * Retrieves an IdentityDegree object to retrieve Identity Degree information from database.
	 * @param cwid
	 * @return
	 */
	IdentityDegree getIdentityDegreeByCwid(String cwid);
}
