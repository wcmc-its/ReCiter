package reciter.database.dao;

import java.util.List;
import java.util.Map;

import reciter.database.model.IdentityGrant;

public interface IdentityGrantDao {
	
	List<IdentityGrant> getIdentityGrantListByCwid(String cwid);

	List<String> getSponsorAwardIdListByCwid(String cwid);

	Map<String, List<reciter.database.mongo.model.Grant>> getAllIdentityGrant();
}
