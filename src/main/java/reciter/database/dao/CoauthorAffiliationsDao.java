package reciter.database.dao;

import java.util.List;

import reciter.database.model.CoauthorAffiliations;

/**
 * DAO class for table rc_identity_education.
 * @author jil3004
 *
 */
public interface CoauthorAffiliationsDao {
	/**
	 * 
	 * @param affiliation
	 * @return
	 */
	CoauthorAffiliations getCoathorAffiliationsByAffiliationLabel(String affiliation);
	
	/**
	 * 
	 * @param affiliations
	 * @return
	 */
	List<CoauthorAffiliations> getCoathorAffiliationsByAffiliationLabel(List<String> affiliations);
}
