package reciter.database.dao;

import java.util.List;

import reciter.database.model.Identity;

public interface IdentityDao {
	/**
	 * 
	 * @param cwid
	 * @return
	 */
	Identity getIdentityByCwid(String cwid);
	/**
	 * 
	 * @param cwid
	 * @return
	 */
	List<Identity> getAssosiatedGrantIdentityList(String cwid);
	
	/**
	 * Returns the pubmed query for this cwid.
	 * @param cwid
	 * @return
	 */
	String getPubmedQuery(String cwid);
	
	/**
	 * Get a list of identity objects based on search string.
	 * @param search
	 * @return
	 */
	List<Identity> getTargetAuthorByNameOrCwid(String search);
}
