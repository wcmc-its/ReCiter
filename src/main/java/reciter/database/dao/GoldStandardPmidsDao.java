package reciter.database.dao;

import java.util.List;

import reciter.database.model.GoldStandardPmid;

/**
 * 
 * @author htadimeti
 *
 */
public interface GoldStandardPmidsDao {

	List<String> getPmidsByCwid(String cwid);
	List<GoldStandardPmid> getGoldStandardPmidsByCwid(String cwid);
}
