package database.dao;

import java.util.List;

import database.model.GoldStandardPmid;

/**
 * 
 * @author htadimeti
 *
 */
public interface GoldStandardPmidsDao {

	List<String> getPmidsByCwid(String cwid);
	List<GoldStandardPmid> getGoldStandardPmidsByCwid(String cwid);
}
