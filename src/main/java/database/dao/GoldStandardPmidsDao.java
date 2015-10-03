package database.dao;

import java.util.List;

import database.model.GoldStandardPmid;

/**
 * 
 * @author htadimeti
 *
 */
public interface GoldStandardPmidsDao {

	public List<String> getPmidsByCwid(String cwid);
	public List<GoldStandardPmid> getGoldStandardPmidsByCwid(String cwid);
}
