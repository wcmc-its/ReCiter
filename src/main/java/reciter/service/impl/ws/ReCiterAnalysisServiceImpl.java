package reciter.service.impl.ws;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.Identity;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ReCiterAnalysisService;
import reciter.service.ScopusService;

@Service("reCiterAnalysisService")
public class ReCiterAnalysisServiceImpl implements ReCiterAnalysisService {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterAnalysisServiceImpl.class);

	private static final String uri = "http://localhost:8080";
	
	@Autowired
	@Qualifier("pubMedServiceWs")
	private PubMedService pubMedService;
	
	@Autowired
	@Qualifier("identityServiceWs")
	private IdentityService identityService;
	
	@Autowired
	@Qualifier("scopusServiceWs")
	private ScopusService scopusService;
	
	@Autowired
	private ESearchResultService eSearchResultService;
	
	@Override
	public Analysis runAnalysis(String cwid) {
		Identity identity = identityService.findByCwid(cwid);
		List<ESearchResult> eSearchResults = eSearchResultService.findByCwid(cwid);
		Set<Long> pmids = new HashSet<Long>();
		for (ESearchResult eSearchResult : eSearchResults) {
			pmids.addAll(eSearchResult.getESearchPmid().getPmids());
		}
		List<Long> pmidList = new ArrayList<Long>(pmids);
		List<PubMedArticle> pubMedArticles = pubMedService.findByPmids(pmidList);
		List<ScopusArticle> scopusArticles = scopusService.findByPubmedId(pmidList);
		
		RestTemplate restTemplate = new RestTemplate();
		String requestUri = uri + "/reciter/run/analysis/";
		slf4jLogger.info("Sending web request with " + pubMedArticles.size() + " PubMed articles. url=" + requestUri);
		
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//      Iterator<PubMedArticle> itr = pubMedArticles.iterator();
//      PubMedArticle p = itr.next();
        HttpEntity<List<PubMedArticle>> entity = new HttpEntity<List<PubMedArticle>>(pubMedArticles, headers);
        ResponseEntity<HttpStatus> response = restTemplate.exchange(requestUri, HttpMethod.PUT, entity, HttpStatus.class);
        // check the response, e.g. Location header,  Status, and body
        slf4jLogger.info("responseBody: " + response.getBody());
//      restTemplate.put(url, p, PubMedArticle.class);
		restTemplate(requestUri, identity, pubMedArticles, scopusArticles);
	}
}
