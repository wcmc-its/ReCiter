package database.dao;

import java.util.List;

import database.model.Identity;

public interface IdentityDao {

	Identity getIdentityByCwid(String cwid);
	List<Identity> getAssosiatedGrantIdentityList(String cwid);
}
