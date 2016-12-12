package reciter.service.impl.ws;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

@Service("pubMedServiceWs")
public class PubMedServiceWsImpl implements PubMedService {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(PubMedServiceWsImpl.class);

	private static final String uri = "http://reciter-pubmed-jpa.heroku.com";
	
	// /reciter/save/pubmed/articles/
	@Override
	public void save(List<PubMedArticle> pubMedArticles) {
		RestTemplate restTemplate = new RestTemplate();
		String requestUri = uri + "/reciter/save/pubmed/articles/";
		slf4jLogger.info("Sending web request with " + pubMedArticles.size() + " PubMed articles. url=" + requestUri);
		restTemplate.put(requestUri, pubMedArticles);
	}
	// /reciter/find/pubmed/articles/pmids
	@Override
	public List<PubMedArticle> findByPmids(List<Long> pmids) {
		RestTemplate restTemplate = new RestTemplate();
		String requestUri = uri + "/reciter/find/pubmed/articles/pmids/?pmids=" + StringUtils.join(pmids, ",");
		slf4jLogger.info("Sending web request with " + pmids.size() + " pmids. url=" + requestUri);
		ResponseEntity<PubMedArticle[]> responseEntity = restTemplate.getForEntity(requestUri, PubMedArticle[].class);
		PubMedArticle[] pubMedArticles = responseEntity.getBody();
		slf4jLogger.info("Received: " + pubMedArticles.length);
		return Collections.emptyList();
	}
}
