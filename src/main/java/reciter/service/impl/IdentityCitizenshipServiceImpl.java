package reciter.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityCitizenshipDao;
import reciter.service.IdentityCitizenshipService;

@Service("identityCitizenshipService")
public class IdentityCitizenshipServiceImpl implements IdentityCitizenshipService{

	@Autowired
	private IdentityCitizenshipDao identityCitizenshipDao;
	
	@Override
	public String getIdentityCitizenshipCountry(String cwid) {
		return identityCitizenshipDao.getIdentityCitizenshipCountry(cwid);
	}
}
