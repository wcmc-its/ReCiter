package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityDirectoryDao;
import reciter.database.model.IdentityDirectory;
import reciter.service.IdentityDirectoryService;

@Service("identityDirectoryService")
public class IdentityDirectoryServiceImpl implements IdentityDirectoryService {

	@Autowired
	private IdentityDirectoryDao identityDirectoryDao;
	
	@Override
	public List<IdentityDirectory> getIdentityDirectoriesByCwid(String cwid) {
		return identityDirectoryDao.getIdentityDirectoriesByCwid(cwid);
	}
}
