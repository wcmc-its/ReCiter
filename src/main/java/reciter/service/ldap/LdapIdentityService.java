package reciter.service.ldap;

import java.util.List;

import reciter.model.identity.Identity;

public interface LdapIdentityService {

	List<Identity> getActiveIdentity();
}
