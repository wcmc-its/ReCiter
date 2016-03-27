package reciter.database.dao;

import java.util.List;

import reciter.database.model.IdentityIntershipsResidencies;

public interface IdentityIntershipsResidenciesDao {
	public List<IdentityIntershipsResidencies> getIdentityIntershipsResidenciesByCwid(String cwid);
}
