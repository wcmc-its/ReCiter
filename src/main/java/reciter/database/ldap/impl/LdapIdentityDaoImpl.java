package reciter.database.ldap.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import reciter.database.oracle.OracleIdentityDao;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Education;
import reciter.model.identity.Identity;
import reciter.model.identity.KnownRelationship;

@Repository("ldapIdentityDao")
public class LdapIdentityDaoImpl implements LdapIdentityDao {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(LdapIdentityDaoImpl.class);

	@Value("${ldap.base.dn}")
	private String ldapbaseDn;

	@Autowired
	private LDAPConnectionFactory lDAPConnectionFactory;

	@Autowired
	private OracleIdentityDao oracleIdentityDao;

	@Override
	public Identity getIdentity(String uid) {
		Identity identity = null;
		List<SearchResultEntry> results = search("(&(objectClass=eduPerson)"
				+ "(|(weillCornellEduPersonTypeCode=academic)"
				+ "(weillCornellEduPersonTypeCode=student-phd-weill)"
				+ "(weillCornellEduPersonTypeCode=student-md-phd-tri-i))"
				+ "(!(weillCornellEduDepartment=Other))"
				+ "(!(weillCornellEduDepartment=NOT APPLICABLE))"
				+ "(!(weillCornellEduDepartment=NA - NA))(weillCornellEduCWID=" + uid + "))");
		slf4jLogger.info("ldap results=" + results);
		if (results.size() == 1) {
			SearchResultEntry entry = results.get(0);
			if(entry.getAttributeValue("weillCornellEduCWID") != null) {
				identity = new Identity();
				// get uid and primary title
				identity.setUid(entry.getAttributeValue("weillCornellEduCWID"));
				slf4jLogger.info("uid retrieved is: " + identity.getUid());
				identity.setTitle(entry.getAttributeValue("weillCornellEduPrimaryTitle"));

				// get primary name which is taken from people OU.
				AuthorName primaryName = new AuthorName(
						entry.getAttributeValue("givenName"),
						entry.getAttributeValue("weillCornellEduMiddleName"),
						entry.getAttributeValue("sn").replace("- M.D.", "")); //Added fix for some surnames having degree in their surnames - M.D.
				identity.setPrimaryName(primaryName);

				// get alternative names for Author Name
				List<AuthorName> alternateNames = new ArrayList<>(searchAlternateNames(identity.getUid(), primaryName));
				identity.setAlternateNames(alternateNames);

				// get email, including data from WOOFA(Personal Email) and Enterprise Directory
				Set<String> uniqueEmails = new HashSet<>();

				// email from ED with ou as people
				if(entry.getAttributeValue("mail") != null && !entry.getAttributeValue("mail").isEmpty()) {
					uniqueEmails.add(entry.getAttributeValue("mail"));
				}

				// email from ED with all other ou
				uniqueEmails.addAll(searchEmails(identity.getUid()));

				// emails from WOOFA
				List<String> emailsFromWoofa = oracleIdentityDao.getPersonalEmailFromOfa(identity.getUid());
				uniqueEmails.addAll(emailsFromWoofa);

				List<String> emails = new ArrayList<>(uniqueEmails);
				identity.setEmails(emails);

				// get Department from ED
				Set<String> departments = new HashSet<String>();
				String deptAttributeValue = entry.getAttributeValue("weillCornellEduDepartment");
				if(deptAttributeValue != null && !deptAttributeValue.isEmpty()) {
					departments.add(deptAttributeValue);
				}

				// get person type code
				String[] personTypeCode = entry.getAttributeValues("weillCornellEduPersonTypeCode");
				List<String> personTypes = new ArrayList<>();
				if (personTypeCode != null && personTypeCode.length != 0) {
					for (String personType : personTypeCode) {
						personTypes.add(personType);
					}
				}
				identity.setPersonTypes(personTypes);
				
				// person type 'students': get the program as well and map to departments
				if ("student-phd-weill".equals(personTypeCode) || "student-md-phd-tri-i".equals(personTypeCode)) {
					departments.addAll(getProgramsForStudents(identity.getUid()));
				}

				identity.setDepartments(new ArrayList<>(departments));
				
				// get list of affiliations
				List<String> institutions = oracleIdentityDao.getInstitutions(identity.getUid());
				identity.setInstitutions(institutions);

				// year of Bachelor Degree
				int bachelorDegreeYear = oracleIdentityDao.getBachelorDegreeYear(identity.getUid());
				int doctoralDegreeYear = oracleIdentityDao.getDoctoralYear(identity.getUid());
				
				Education education = new Education();
				education.setBachelorYear(bachelorDegreeYear);
				education.setDoctoralYear(doctoralDegreeYear);
				identity.setDegreeYear(education);
				
				//List of grants
				List<String> grants = oracleIdentityDao.getGrants(identity.getUid());
				identity.setGrants(grants);
				
				//List of relationship name from ED
				List<String> knownRelationshipCwids = oracleIdentityDao.getRelationshipCwids(identity.getUid());
				List<KnownRelationship> knownRelationships = new ArrayList<>();
				for (String relationshipCwid : knownRelationshipCwids) {
					AuthorName authorName = searchByCwid(relationshipCwid);
					if (authorName != null) {
						KnownRelationship knownRelationship = new KnownRelationship();
						knownRelationship.setUid(relationshipCwid);
						knownRelationship.setName(authorName);
						knownRelationship.setType("co-investigator");
						knownRelationships.add(knownRelationship);
					}
				}
				identity.setKnownRelationships(knownRelationships);
				
				identity.setPubMedAlias(Collections.emptyList());
			}
		}
		return identity;
	}

	private Set<AuthorName> searchAlternateNames(String uid, AuthorName primaryName) {

		Set<AuthorName> alternateNames = new HashSet<AuthorName>();
		String filter = "(&(objectClass=weillCornellEduSORRecord)(weillCornellEduCWID=" + uid + "))";
		List<SearchResultEntry> results = searchWithBaseDN(filter, "ou=sors,dc=weill,dc=cornell,dc=edu");
		for (SearchResultEntry entry : results) {
			AuthorName authorName = new AuthorName(
					entry.getAttributeValue("givenName"),
					entry.getAttributeValue("weillCornellEduMiddleName"),
					entry.getAttributeValue("sn").replace("- M.D.", "")); //Added fix for some surnames having degree in their surnames - M.D.
			if (!primaryName.equals(authorName)) {
				alternateNames.add(authorName);
			}
		}
		return alternateNames;
	}

	private Set<String> searchEmails(String uid) {
		String filter = "(&(objectClass=weillCornellEduSORRecord)(weillCornellEduCWID=" + uid + "))";
		List<SearchResultEntry> results = searchWithBaseDN(filter, "ou=sors,dc=weill,dc=cornell,dc=edu");
		Set<String> emails = new HashSet<>();
		if (results != null) {
			for (SearchResultEntry entry : results) {
				if (entry.getAttributeValue("mail") != null && !entry.getAttributeValue("mail").isEmpty()) {
					emails.add(entry.getAttributeValue("mail"));
				}
			}
		}
		return emails;
	}

	private Set<String> getProgramsForStudents(String uid) {
		Set<String> departments = new HashSet<>();
		String filter = "(&(objectClass=weillCornellEduSORRecord)(weillCornellEduCWID=" + uid + "))";
		List<SearchResultEntry> results = searchWithBaseDN(filter, "ou=students,ou=sors,dc=weill,dc=cornell,dc=edu");
		for (SearchResultEntry entry : results) {
			if(entry.getAttributeValue("weillCornellEduProgram") != null && !entry.getAttributeValue("weillCornellEduProgram").isEmpty()) {
				departments.add(entry.getAttributeValue("weillCornellEduProgram"));
			}
		}
		return departments;

	}
	
	private void getHrRelationship(String uid) {
		String filter = "| ldapsearch domain=ED search='(&(objectClass=eduPerson)"
				+ "(|(weillCornellEduPersonTypeCode=academic-faculty-weillfulltime)"
				+ "(weillCornellEduPersonTypeCode=academic-faculty-weillparttime)"
				+ "(weillCornellEduPersonTypeCode=student-phd-*)"
				+ "(weillCornellEduPersonTypeCode=student-md-phd-tri-i)))' attrs='weillCornellEduCWID' "
				+ "| fields weillCornellEduCWID "
				+ "| join weillCornellEduCWID type=inner ["
				+ "| ldapsearch domain=ED search='(&(ou=employees)"
				+ "(objectClass=weillCornellEduSORRecord))' "
				+ "attrs='weillCornellEduCWID,weillCornellEduPrimaryDepartment,weillCornellEduPrimaryDepartmentCode' "
				+ "| fields weillCornellEduCWID,weillCornellEduPrimaryDepartment,weillCornellEduPrimaryDepartmentCode] "
				+ "| rename weillCornellEduCWID as targetCWID "
				+ "| join weillCornellEduPrimaryDepartmentCode type=inner ["
				+ "| ldapsearch domain=ED search='(&(ou=employees)(objectClass=weillCornellEduSORRecord))' "
				+ "attrs='weillCornellEduPrimaryDepartmentCode,weillCornellEduCWID' "
				+ "| stats dc(weillCornellEduCWID) AS count by weillCornellEduPrimaryDepartmentCode "
				+ "| where count < 90] "
				+ "| join weillCornellEduPrimaryDepartmentCode type=left max=0 ["
				+ "| ldapsearch domain=ED search='(&(ou=employees)(objectClass=weillCornellEduSORRecord))' "
				+ "attrs='weillCornellEduPrimaryDepartmentCode,weillCornellEduCWID,sn,givenName' "
				+ "| rename weillCornellEduCWID AS relationshipCWID "
				+ "| fields relationshipCWID, weillCornellEduPrimaryDepartmentCode, sn, givenName] "
				+ "| fields - _* "
				+ "| fields  targetCWID, relationshipCWID, sn, givenName, count, "
				+ "weillCornellEduPrimaryDepartmentCode, weillCornellEduPrimaryDepartment "
				+ "| where targetCWID != relationshipCWID";
		List<SearchResultEntry> results = searchWithBaseDN(filter, "ou=students,ou=sors,dc=weill,dc=cornell,dc=edu");
		for (SearchResultEntry entry : results) {
			if(entry.getAttributeValue("firstname") != null && !entry.getAttributeValue("firstname").isEmpty()) {
				
			}
		}
	}
	
	/**
	 * Search LDAP to retrieve name for a given uid.
	 * 
	 * @param uid
	 * 
	 * @return AuthorName if found, otherwise null.
	 */
	private AuthorName searchByCwid(String uid) {
		String filter = "(&(objectClass=eduPerson)" + "(weillCornellEduCWID=" + uid + "))";
		List<SearchResultEntry> results = searchWithBaseDN(filter, "ou=people,dc=weill,dc=cornell,dc=edu");
		if (results != null && !results.isEmpty()) {
			SearchResultEntry entry = results.get(0);
			if(entry.getAttributeValue("weillCornellEduCWID") != null && !entry.getAttributeValue("weillCornellEduCWID").isEmpty()) {
				return new AuthorName(entry.getAttributeValue("givenName"),
						entry.getAttributeValue("weillCornellEduMiddleName"),
						entry.getAttributeValue("sn").replace("- M.D.", ""));
			}
		}
		return null;
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
