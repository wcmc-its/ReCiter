package database.dao;

import database.model.IdentityDegree;

public interface IdentityDegreeDao {

	IdentityDegree getIdentityDegreeByCwid(String cwid);
}
