package database.dao;

import java.util.List;

import database.model.IdentityDirectory;

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
	public List<IdentityDirectory> getIdentityDirectoriesByCwid(String cwid);
}
