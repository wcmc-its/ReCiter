package reciter.database.dao;

import java.util.List;

import reciter.database.model.IdentityDirectory;

/**
 * 
 * @author htadimeti
 *
 */
public interface IdentityDirectoryDao {
	/**
	 * 
	 * @param cwid
	 * @return
	 */
	List<IdentityDirectory> getIdentityDirectoriesByCwid(String cwid);
}
