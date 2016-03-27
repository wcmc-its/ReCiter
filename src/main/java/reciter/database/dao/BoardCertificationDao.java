package reciter.database.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author htadimeti
 *
 */
public interface BoardCertificationDao {
	
	List<String> getBoardCertificationsByCwid(String cwid);
	public Map<String, List<String>> getBoardCertificationsMapByCwid(String cwid) throws SQLException;
}
