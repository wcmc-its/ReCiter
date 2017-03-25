package reciter.service.ldap.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.ldap.LdapIdentityDao;
import reciter.model.identity.Identity;
import reciter.service.ldap.LdapIdentityService;

@Service("ldapIdentityService")
public class LdapIdentityServiceImpl implements LdapIdentityService {

	@Autowired
	private LdapIdentityDao ldapIdentityDao;

	@Override
	public Identity getIdentity(String cwid) {
		return ldapIdentityDao.getIdentity(cwid);
	}
}
