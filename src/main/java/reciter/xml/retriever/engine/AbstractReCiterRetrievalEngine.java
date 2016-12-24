package reciter.xml.retriever.engine;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.mongo.model.ESearchPmid;
import reciter.database.mongo.model.ESearchResult;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.mongo.ESearchResultService;
import reciter.service.mongo.IdentityService;
import reciter.service.mongo.PubMedService;
import reciter.service.mongo.ScopusService;
import reciter.xml.retriever.pubmed.AffiliationInDbRetrievalStrategy;
import reciter.xml.retriever.pubmed.AffiliationRetrievalStrategy;
import reciter.xml.retriever.pubmed.DepartmentRetrievalStrategy;
import reciter.xml.retriever.pubmed.EmailRetrievalStrategy;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.GrantRetrievalStrategy;
import reciter.xml.retriever.pubmed.PubMedQueryResult;

@Component("abstractReCiterRetrievalEngine")
public abstract class AbstractReCiterRetrievalEngine implements ReCiterRetrievalEngine {

	@Autowired
	protected PubMedService pubMedService;

	@Autowired
	protected ESearchResultService eSearchResultService;

	@Autowired
	protected ScopusService scopusService;

	@Autowired
	protected IdentityService identityService;
	
	@Autowired
	protected AffiliationInDbRetrievalStrategy affiliationInDbRetrievalStrategy;
	
	@Autowired
	protected AffiliationRetrievalStrategy affiliationRetrievalStrategy;
	
	@Autowired
	protected DepartmentRetrievalStrategy departmentRetrievalStrategy;
	
	@Autowired
	protected EmailRetrievalStrategy emailRetrievalStrategy;
	
	@Autowired
	protected FirstNameInitialRetrievalStrategy firstNameInitialRetrievalStrategy;
	
	@Autowired
	protected GrantRetrievalStrategy grantRetrievalStrategy;
	
	/**
	 * Save the PubMed articles and the ESearch results.
	 * @param pubMedArticles
	 * @param cwid
	 */
	protected void savePubMedArticles(Collection<PubMedArticle> pubMedArticles, String cwid, String retrievalStrategyName, List<PubMedQueryResult> pubMedQueryResults) {
		// Save the articles.
		List<PubMedArticle> pubMedArticleList = new ArrayList<>(pubMedArticles);
		pubMedService.save(pubMedArticleList);

		// Save the search result.
		List<Long> pmids = new ArrayList<Long>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
		}
		ESearchPmid eSearchPmid = new ESearchPmid(pmids, retrievalStrategyName, LocalDateTime.now(Clock.systemUTC()));
//		boolean exist = eSearchResultService.existByCwidAndRetrievalStrategyName(cwid, eSearchPmid.getRetrievalStrategyName());
//		if (exist) {
//			eSearchResultService.update(new ESearchResult(cwid, eSearchPmid, pubMedQueryResults));
//		} else {
		eSearchResultService.save(new ESearchResult(cwid, eSearchPmid, pubMedQueryResults));
//		}
	}
}
