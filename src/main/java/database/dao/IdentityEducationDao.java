package database.dao;

import java.util.List;

import database.model.IdentityEducation;

public interface IdentityEducationDao {

	List<IdentityEducation> getEducation(String cwid);
	List<String> getIdentityCitizenshipEducation(String cwid);
}
