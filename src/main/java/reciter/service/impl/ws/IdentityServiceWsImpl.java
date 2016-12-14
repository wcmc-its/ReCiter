package reciter.service.impl.ws;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import reciter.database.mongo.model.Identity;
import reciter.service.IdentityService;

@Service("identityServiceWs")
public class IdentityServiceWsImpl implements IdentityService {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(IdentityServiceWsImpl.class);

	private static final String uri = "https://reciter-jpa.herokuapp.com";
	
	@Override
	public void save(List<Identity> identities) {
		RestTemplate restTemplate = new RestTemplate();
		String requestUri = uri + "/reciter/save/identities/";
		slf4jLogger.info("Sending web request with " + identities.size() + " identities. url=" + requestUri);
		restTemplate.put(requestUri, identities);
	}

	@Override
	public Identity save(Identity identity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identity findByCwid(String cwid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Identity> findByCwidRegex(String search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updatePubMedAlias(Identity identity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Identity identity) {
		// TODO Auto-generated method stub
		
	}

}
