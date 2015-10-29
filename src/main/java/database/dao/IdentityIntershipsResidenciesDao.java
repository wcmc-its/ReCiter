package database.dao;

import java.util.List;

import database.model.IdentityIntershipsResidencies;

public interface IdentityIntershipsResidenciesDao {
	public List<IdentityIntershipsResidencies> getIdentityIntershipsResidenciesByCwid(String cwid);
}
