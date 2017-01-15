package reciter.service.ldap;

import reciter.model.identity.Identity;

public interface LdapIdentityService {

	Identity getIdentity(String uid);
}
