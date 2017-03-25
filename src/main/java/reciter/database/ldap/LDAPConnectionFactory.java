package reciter.database.ldap;

import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

@Component
public class LDAPConnectionFactory {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(LDAPConnectionFactory.class);
	
	@Value("${ldap.bind.dn}")
    private String ldapBindDn;
    
    @Value("${ldap.port}")
    private Integer ldapPort;
    
    @Value("${ldap.bind.password}")
    private String ldapBindPassword;
    
    @Value("${ldap.hostname}")
    private String ldapHostname;

    public LDAPConnection createConnection() {
		
		LDAPConnection connection = null;
		try {
			SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
			connection = new LDAPConnection(sslUtil.createSSLSocketFactory());
			connection.connect(ldapHostname, ldapPort);
			connection.bind(ldapBindDn, ldapBindPassword);
			
		} catch (LDAPException e) {
			slf4jLogger.error("LDAPConnection error", e);
		} catch (GeneralSecurityException e) {
			slf4jLogger.error("Error connecting via SSL to LDAP", e);
		}
		return connection;
	}
}
