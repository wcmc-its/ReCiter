package reciter.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityDegreeDao;
import reciter.database.model.IdentityDegree;
import reciter.service.IdentityDegreeService;

@Service("identityDegreeService")
public class IdentityDegreeServiceImpl implements IdentityDegreeService {

	@Autowired
	private IdentityDegreeDao identityDegreeDao;
	
	@Override
	public IdentityDegree getIdentityDegreeByCwid(String cwid) {
		return identityDegreeDao.getIdentityDegreeByCwid(cwid);
	}
}
