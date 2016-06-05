package reciter.service;

import reciter.database.model.IdentityDegree;

public interface IdentityDegreeService {

	IdentityDegree getIdentityDegreeByCwid(String cwid);
}
