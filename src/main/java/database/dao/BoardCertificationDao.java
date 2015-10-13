package database.dao;

import java.util.List;

/**
 * 
 * @author htadimeti
 *
 */
public interface BoardCertificationDao {
	
	List<String> getBoardCertificationsByCwid(String cwid);
}
