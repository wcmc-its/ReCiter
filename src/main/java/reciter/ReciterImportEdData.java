package reciter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

public class ReciterImportEdData {

	private static final Logger log = LoggerFactory.getLogger(ReciterImportEdData.class);

	//Ldap Variables
	private String bindDN = null;
	private String bindPassword = null;
	private String ldapHostname = null;
	private int ldapPort = 0;
	private String baseDN = null; 

	//OFA Variables
	private String url = null;
	private String username = null;
	private String password = null;

	private Properties props = new Properties();

	/**
	 * The LDAP connection gets saved in this variable
	 */
	private LDAPConnection conn = null;

	/**
	 * Main method
	 * 
	 * @param args
	 *            command-line arguments
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main (String args[]) {
		if (args.length == 0) {
			log.info("Usage: java fetch.JSONPeopleFetch [properties filename]");
			log.info("e.g. java fetch.JSONPeopleFetch /usr/share/vivo-ed-people/examples/wcmc_people.properties");
		} else if (args.length == 1) { // path of the properties file
			new ReciterImportEdData().init(args[0]);
		}
	}


	private void init(String propertiesFile) {

		// get today's date
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();

		try {
			props.load(new FileInputStream(propertiesFile));
		} catch (FileNotFoundException e) {
			log.info("File not found error: " + e);
		} catch (IOException e) {
			log.info("IOException error: " + e);
		}

		// OFA database
		this.url = this.props.getProperty("Fetch.database.url");
		this.username = this.props.getProperty("Fetch.database.username");
		this.password = this.props.getProperty("Fetch.database.password");


		//ED API 
		this.bindDN = this.props.getProperty("bindDN");
		this.bindPassword = this.props.getProperty("bindPassword");
		this.baseDN = this.props.getProperty("ldapBaseDN");
		this.ldapHostname = this.props.getProperty("ldapHostname");
		this.ldapPort = Integer.parseInt(this.props.getProperty("ldapPort"));



		try {
			execute();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void execute() throws IOException {

		getActivePeopleFromED();

	}

	private void getActivePeopleFromED() {

		String filter = "(&(objectClass=eduPerson)(|(weillCornellEduPersonTypeCode=academic)(weillCornellEduPersonTypeCode=student-phd-weill)(weillCornellEduPersonTypeCode=student-phd-weill)(weillCornellEduPersonTypeCode=student-md-phd-tri-i))(weillCornellEduCWID=ccole))";

		List<SearchResultEntry> results = search(filter);

		if (results != null) {

			for (SearchResultEntry entry : results) {
				if(entry.getAttributeValue("weillCornellEduCWID") != null) {
					System.out.println(entry.getAttributeValue("weillCornellEduCWID") + " " + entry.getAttributeValue("weillCornellEduPrimaryTitle") + " " + entry.getAttributeValue("sn"));
					/*filter = "ou=people";
		            		List<SearchResultEntry> nameTitle = searchWithBaseDN(filter, "dc=weill,dc=cornell,dc=edu");
		            		if (results != null) {

		        	            for (SearchResultEntry name : results) {


		        	            	}
		        	            }*/

				}
			}

			log.info("Number of results found: " + results.size() +
					"\n");
		}
		else
			log.info("No results found");
	}

	/**
	 * Prints out the name and value of the provided attribute.
	 *
	 * @param entry        A SearchResultEntry entry
	 * @param attribute    The name of the attribute that you want printed
	 */
	protected void printAttributeValues(
			final SearchResultEntry entry, final String attribute) {
		final String[] values = entry.getAttributeValues(attribute);
		if (values != null && values.length > 0) {
			for (String value : values) {
				System.out.println(attribute + ": " + value);
			}
		}
	}

	/**
	 * Searches the ED for the provided filter.
	 *
	 * @param filter        A valid LDAP filter string
	 * @return a {@code List} of {@code SearchResultEntry} objects.
	 */
	protected List<SearchResultEntry> search(final String filter) {
		return search(filter, this.baseDN, SearchScope.SUBORDINATE_SUBTREE,
				"*","modifyTimestamp");
	}

	/**
	 * Searches the ED for the provided filter.
	 *
	 * @param filter        A valid LDAP filter string
	 * @return a {@code List} of {@code SearchResultEntry} objects.
	 */
	protected List<SearchResultEntry> searchWithBaseDN(final String filter, String basedn) {
		return search(filter, basedn, SearchScope.SUBORDINATE_SUBTREE,
				"*","modifyTimestamp");
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
	protected List<SearchResultEntry> search(
			final String filter, final String base, SearchScope scope,
			String... attributes) {

		try {
			SearchRequest searchRequest = new SearchRequest(
					base,
					scope,
					filter,
					attributes);

			SearchResult results = conn().search(searchRequest);
			List<SearchResultEntry> entries = results.getSearchEntries();
			return entries;
		}
		catch (LDAPSearchException lse) {
			log.info("LDAPSearchException: " + lse.toString());
		}
		catch (LDAPException e) {
			log.info("LDAPException: " + e.toString());
		}
		return new LinkedList<SearchResultEntry>();
	}






	/**
	 * Creates and returns an LDAPConnection from the values of the
	 * connection properties.
	 */
	private LDAPConnection conn() {

		if (this.conn != null) {
			return this.conn;
		}
		else {
			try {
				SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
				this.conn = new LDAPConnection(sslUtil.createSSLSocketFactory());
				this.conn.connect(this.ldapHostname, this.ldapPort);
				this.conn.bind(this.bindDN, this.bindPassword);
				log.info("Connection success");
				return this.conn;

			}
			catch (LDAPException e) {
				log.info("LDAPConnection error: " + e.toString());
				return null;
			}
			catch (GeneralSecurityException gse) {
				log.info("Error connecting via SSL to LDAP: " +
						gse.toString());
				return null;
			}
		}
	}

	/**
	 * Disconnct the LDAPConnection.
	 */
	private void disconnect() {
		if (this.conn != null) {
			this.conn.close();
		}
	}

}