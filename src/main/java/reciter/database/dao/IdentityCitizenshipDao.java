package reciter.database.dao;

/**
 * DAO class for table rc_identity_citizenship.
 * @author jil3004
 *
 */
public interface IdentityCitizenshipDao {

	// TODO: @Balu and @Hanumantha: Please create functions to get rows from the database table "rc_identity_citizenship".
	/* Function to get the identity citizen country from database table  
	 * @return country 
	 */
	String getIdentityCitizenshipCountry(String cwid);

}