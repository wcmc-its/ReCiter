package reciter.xml.retriever.engine;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.mongo.model.ESearchPmid;
import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.xml.parser.scopus.model.ScopusArticle;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

@Component("defaultReCiterRetrievalEngine")
public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(DefaultReCiterRetrievalEngine.class);

	@Autowired
	private PubMedService pubMedService;

	@Autowired
	private ESearchResultService eSearchResultService;
	
	@Autowired
	private ScopusService scopusService;
	
	@Override
	public List<Long> retrieve(Identity identity) {

		List<RetrievalStrategy> retrievalStrategies = new  ArrayList<RetrievalStrategy>();
		
		// Retrieve by email.
//		RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy(false);
		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy(false);
//		RetrievalStrategy departmentRetrievalStrategy = new DepartmentRetrievalStrategy(false);
//		RetrievalStrategy affiliationInDbRetrievalStrategy = new AffiliationInDbRetrievalStrategy(false);
//		RetrievalStrategy grantRetrievalStrategy = new GrantRetrievalStrategy(false);
		
//		retrievalStrategies.add(emailRetrievalStrategy);
		retrievalStrategies.add(firstNameInitialRetrievalStrategy);
//		retrievalStrategies.add(departmentRetrievalStrategy);
//		retrievalStrategies.add(affiliationInDbRetrievalStrategy);
//		retrievalStrategies.add(grantRetrievalStrategy);
		
		return retrieve(retrievalStrategies, identity);
	}
	
	/**
	 * Retrieve articles.
	 * @param retrievalStrategies
	 * @param targetAuthor
	 * @return
	 */
	private List<Long> retrieve(List<RetrievalStrategy> retrievalStrategies, Identity identity) {
		String cwid = identity.getCwid();
		List<Long> pmids = new ArrayList<Long>();
		for (RetrievalStrategy retrievalStrategy : retrievalStrategies) {
			try {
				retrievalStrategy.constructPubMedQuery(identity);
				slf4jLogger.error("cwid=[" + cwid + "], retrievalStrategy=[" + retrievalStrategy.getRetrievalStrategyName() 
					+ "], pubmedQuery=[" + retrievalStrategy.getPubMedQuery() + "]");
				
				List<Long> strategyPmids = new ArrayList<Long>();
				int numberOfPubmedArticles = retrievalStrategy.getNumberOfPubmedArticles();
				if (numberOfPubmedArticles > 0) {
					List<PubMedArticle> pubMedArticles = retrievalStrategy.retrieve();
					for (PubMedArticle pubMedArticle : pubMedArticles) {
						strategyPmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
					}
					savePubMedArticles(pubMedArticles, cwid, retrievalStrategy.getRetrievalStrategyName());
				}
				List<ScopusArticle> scopusArticles = retrievalStrategy.retrieveScopus(strategyPmids);
				scopusService.save(scopusArticles);
				
				pmids.addAll(strategyPmids);
			} catch (IOException e) {
				slf4jLogger.error("RetrievalStrategy " + retrievalStrategy + "encountered an IO Exception", e);
			}
		}
		return pmids;
	}
	
	/**
	 * Save the PubMed articles and the ESearch results.
	 * @param pubMedArticles
	 * @param cwid
	 */
	private void savePubMedArticles(List<PubMedArticle> pubMedArticles, String cwid, String retrievalStrategyName) {
		// Save the articles.
		pubMedService.save(pubMedArticles);

		// Save the search result.
		List<Long> pmids = new ArrayList<Long>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
		}
		ESearchPmid eSearchPmid = new ESearchPmid(pmids, retrievalStrategyName, LocalDateTime.now());
		eSearchResultService.save(new ESearchResult(cwid, eSearchPmid));
	}

//	public Set<AuthorName> findUniqueAuthorsWithSameLastNameAsTargetAuthor(TargetAuthor targetAuthor) {
//		Set<AuthorName> uniqueAuthors = new HashSet<AuthorName>();
//		ESearchResult eSearchResult = eSearchResultService.findByCwid(targetAuthor.getCwid());
//		List<Long> pmids = eSearchResult.getPmids();
//		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(pmids);
//		String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();
//
//		slf4jLogger.info("number of articles=[" + pubMedArticles.size() + "].");
//
//		for (PubMedArticle pubMedArticle : pubMedArticles) {
//			if (pubMedArticle.getMedlineCitation().getArticle() != null && 
//					pubMedArticle.getMedlineCitation().getArticle().getAuthorList() != null) {
//
//				slf4jLogger.info(pubMedArticle.getMedlineCitation().getArticle().getAuthorList() + " ");
//
//				List<MedlineCitationArticleAuthor> authors = pubMedArticle.getMedlineCitation().getArticle().getAuthorList();
//				for (MedlineCitationArticleAuthor author : authors) {
//					String lastName = author.getLastName();
//					if (targetAuthorLastName.equals(lastName)) {
////						AuthorName authorName = getAuthorName(author);
////						uniqueAuthors.add(authorName);
//					}
//				}
//			} else {
//				slf4jLogger.info(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid() + "");
//			}
//		}
//		return uniqueAuthors;
//	}

	public void updatePubMedQuery(RetrievalStrategy retrievalStrategy) throws IOException {

		String pubMedQuery = retrievalStrategy.getPubMedQuery();
		int numberOfPubmedArticles = retrievalStrategy.getNumberOfPubmedArticles();

		String[] pmidStrings = retrievalStrategy.retrievePmids(pubMedQuery);
		List<Long> pmids = new ArrayList<Long>();
		for (String pmidString : pmidStrings) {
			pmids.add(Long.valueOf(pmidString));
		}
		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(pmids);

		if (pubMedArticles != null && !pubMedArticles.isEmpty()) {
			String decodedeUrl = URLDecoder.decode(pubMedQuery, "UTF-8");
			if (pubMedArticles.size() == 1) {
				retrievalStrategy.setPubMedQuery(decodedeUrl + " NOT " + pubMedArticles.get(0).getMedlineCitation().getMedlineCitationPMID().getPmid() + "[pmid]");
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append("(");
				int index = 0;
				for (PubMedArticle pubMedArticle : pubMedArticles) {
					if (index != pubMedArticles.size() - 1) {
						sb.append(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid() + "[pmid] OR ");
					} else {
						sb.append(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid() + "[pmid]");
					}
					index++;
				}
				sb.append(")");
				retrievalStrategy.setPubMedQuery(decodedeUrl + " NOT " + sb.toString());
			}
			retrievalStrategy.setNumberOfPubmedArticles(numberOfPubmedArticles - pubMedArticles.size());
		}

		slf4jLogger.info("Updated PubMed query=[" + retrievalStrategy.getPubMedQuery() 
		+ "], number of articles = [" + retrievalStrategy.getNumberOfPubmedArticles() + "]");
	}
}
