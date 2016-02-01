package database.dao;

import java.util.List;

import database.model.Identity;

/**
 * 
 * @author htadimeti
 *
 */
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
}
