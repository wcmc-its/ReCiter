package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityAlternateDeptNamesDao;
import reciter.service.IdentityAlternateDeptNamesService;

@Service("identityAlternateDeptNamesService")
public class IdentityAlternateDeptNamesServiceImpl implements IdentityAlternateDeptNamesService {

	@Autowired
	private IdentityAlternateDeptNamesDao identityAlternateDeptNamesDao;
	
	@Override
	public List<String> getAlternateNames(String nameOfa) {
		return identityAlternateDeptNamesDao.getAlternateNames(nameOfa);
	}
}
