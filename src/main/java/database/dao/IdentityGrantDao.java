package database.dao;

import java.util.List;

import database.model.IdentityGrant;

public interface IdentityGrantDao {
	List<IdentityGrant> getIdentityGrantListByCwid(String cwid);
}
