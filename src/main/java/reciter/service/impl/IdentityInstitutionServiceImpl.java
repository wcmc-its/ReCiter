package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityInstitutionDao;
import reciter.service.IdentityInstitutionService;

@Service("identityInstitutionService")
public class IdentityInstitutionServiceImpl implements IdentityInstitutionService {

	@Autowired
	private IdentityInstitutionDao identityInstitutionDao;

	@Override
	public List<String> getInstitutionByCwid(String cwid) {
		return identityInstitutionDao.getInstitutionByCwid(cwid);
	}
}
