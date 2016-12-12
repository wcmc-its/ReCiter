package reciter.service.impl.ws;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ScopusService;

@Service("scopusServiceWs")
public class ScopusServiceWsImpl implements ScopusService {
	
	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusServiceWsImpl.class);

//	private static final String uri = "http://reciter-scopus-jpa.heroku.com";
	private static final String uri = "http://localhost:8080";
	
	@Override
	public void save(List<ScopusArticle> scopusArticles) {
		RestTemplate restTemplate = new RestTemplate();
		String requestUri = uri + "/reciter/save/scopus/articles/";
		slf4jLogger.info("Sending web request with " + scopusArticles.size() + " Scopus articles. url=" + requestUri);
		restTemplate.put(requestUri, scopusArticles);
	}

	@Override
	public List<ScopusArticle> findByPubmedId(List<Long> pmids) {
		RestTemplate restTemplate = new RestTemplate();
		String requestUri = uri + "/reciter/find/scopus/articles/pmids/?pmids=" + StringUtils.join(pmids, ",");
		slf4jLogger.info("Sending web request with " + pmids.size() + " pmids. url=" + requestUri);
		ResponseEntity<ScopusArticle[]> responseEntity = restTemplate.getForEntity(requestUri, ScopusArticle[].class);
		ScopusArticle[] scopusArticles = responseEntity.getBody();
		slf4jLogger.info("Received: " + scopusArticles.length);
		return Arrays.asList(scopusArticles);
	}

}
