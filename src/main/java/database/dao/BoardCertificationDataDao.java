package database.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author htadimeti
 *
 */
public interface BoardCertificationDataDao {
	/**
	 * 
	 * @param cwid
	 * @return
	 * @throws SQLException
	 */
	public Map<String, List<String>> getBoardCertificationsByCwid(String cwid) throws SQLException;
}
