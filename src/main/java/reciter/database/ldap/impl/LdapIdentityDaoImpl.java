package reciter.database.ldap.impl;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import reciter.database.ldap.LDAPConnectionFactory;
import reciter.database.ldap.LdapIdentityDao;
import reciter.model.identity.Identity;

@Repository("ldapIdentityDao")
public class LdapIdentityDaoImpl implements LdapIdentityDao {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(LdapIdentityDaoImpl.class);

	@Value("${ldap.base.dn}")
	private String ldapbaseDn;

	@Value("${ldap.filter}")
	private String ldapFilter;
	
	@Autowired
	private LDAPConnectionFactory lDAPConnectionFactory;

	public List<Identity> getActiveIdentity() {
		List<SearchResultEntry> results = search(ldapFilter);
		for (SearchResultEntry entry : results) {
			if(entry.getAttributeValue("weillCornellEduCWID") != null) {
				// TODO create identity information
				slf4jLogger.info(entry.getAttributeValue("weillCornellEduCWID") + " " + 
						entry.getAttributeValue("weillCornellEduPrimaryTitle") + " " + entry.getAttributeValue("sn"));
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Searches the ED for the provided filter.
	 *
	 * @param filter A valid LDAP filter string
	 * @return a {@code List} of {@code SearchResultEntry} objects.
	 */
	protected List<SearchResultEntry> search(final String filter) {
		return search(filter, ldapbaseDn, SearchScope.SUBORDINATE_SUBTREE, "*","modifyTimestamp");
	}

	/**
	 * Searches the ED for the provided filter.
	 *
	 * @param filter A valid LDAP filter string
	 * @return a {@code List} of {@code SearchResultEntry} objects.
	 */
	protected List<SearchResultEntry> searchWithBaseDN(final String filter, String basedn) {
		return search(filter, basedn, SearchScope.SUBORDINATE_SUBTREE, "*","modifyTimestamp");
	}

	/**
	 * Searches the ED for the provided filter.
	 *
	 * @param filter        A valid LDAP filter string
	 * @param base          The LDAP search base you want to use
	 * @param scope         A SearchScope that you want to use
	 * @param attributes    A list of attributes that you want returned from
	 * LDAP
	 * @return a {@code List} of {@code SearchResultEntry} objects.
	 */
	protected List<SearchResultEntry> search(final String filter, final String base, SearchScope scope, String... attributes) {
		LDAPConnection connection = null;
		try {
			SearchRequest searchRequest = new SearchRequest(base, scope, filter, attributes);
			connection = lDAPConnectionFactory.createConnection();
			SearchResult results = connection.search(searchRequest);
			List<SearchResultEntry> entries = results.getSearchEntries();
			return entries;
		} catch (LDAPSearchException e) {
			slf4jLogger.error("LDAPSearchException", e);
		} catch (LDAPException e) {
			slf4jLogger.error("LDAPException", e);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return Collections.emptyList();
	}
}
