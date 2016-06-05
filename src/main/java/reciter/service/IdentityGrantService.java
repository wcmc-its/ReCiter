package reciter.service;

import java.util.List;

import reciter.database.model.IdentityGrant;

public interface IdentityGrantService {

	List<IdentityGrant> getIdentityGrantListByCwid(String cwid);

	List<String> getSponsorAwardIdListByCwid(String cwid);
}
