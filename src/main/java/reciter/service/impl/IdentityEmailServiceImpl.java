package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityEmailDao;
import reciter.service.IdentityEmailService;

@Service("identityEmailService")
public class IdentityEmailServiceImpl implements IdentityEmailService {

	@Autowired
	private IdentityEmailDao identityEmailDao;
	
	@Override
	public List<String> getEmailAddressesForCwid(String cwid) {
		return identityEmailDao.getEmailAddressesForCwid(cwid);
	}
}
