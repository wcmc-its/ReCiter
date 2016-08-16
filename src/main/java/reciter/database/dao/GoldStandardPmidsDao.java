package reciter.database.dao;

import java.util.List;
import java.util.Map;

import reciter.database.model.GoldStandardPmid;

/**
 * 
 * @author htadimeti
 *
 */
public interface GoldStandardPmidsDao {

	List<Long> getPmidsByCwid(String cwid);
	List<GoldStandardPmid> getGoldStandardPmidsByCwid(String cwid);
	Map<String, List<Long>> getGoldStandard();
}
