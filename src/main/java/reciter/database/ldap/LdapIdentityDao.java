package reciter.database.ldap;

import java.util.List;

import reciter.model.identity.Identity;

public interface LdapIdentityDao {

	List<Identity> getActiveIdentity();
}
