package reciter.database.ldap;

import reciter.model.identity.Identity;

public interface LdapIdentityDao {

	Identity getIdentity(String uid);
}
