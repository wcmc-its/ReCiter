package reciter.database.dao;

import java.util.List;

import reciter.database.model.IdentityGrant;

public interface IdentityGrantDao {
	List<IdentityGrant> getIdentityGrantListByCwid(String cwid);

	List<String> getSponsorAwardIdListByCwid(String cwid);
}
