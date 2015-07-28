package database.dao;

import java.util.List;

public interface GoldStandardPmidsDao {

	List<String> getPmidsByCwid(String cwid);
}
