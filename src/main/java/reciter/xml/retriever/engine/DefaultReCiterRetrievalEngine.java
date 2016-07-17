package reciter.xml.retriever.engine;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.mongo.model.ESearchResult;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import reciter.model.converter.PubMedConverter;
import reciter.model.pubmed.MedlineCitationArticleAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.PubMedService;
import reciter.utils.AuthorNameUtils;
import reciter.xml.retriever.pubmed.AffiliationInDbRetrievalStrategy;
import reciter.xml.retriever.pubmed.DepartmentRetrievalStrategy;
import reciter.xml.retriever.pubmed.EmailRetrievalStrategy;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

@Component("defaultReCiterRetrievalEngine")
public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(DefaultReCiterRetrievalEngine.class);

	@Autowired
	private PubMedService pubMedService;

	@Autowired
	private ESearchResultService eSearchResultService;
	
	@Override
	public List<Long> retrieve(TargetAuthor targetAuthor) {

		List<RetrievalStrategy> retrievalStrategies = new  ArrayList<RetrievalStrategy>();
		
		// Retrieve by email.
		RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy(false);
		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy(false);
		RetrievalStrategy departmentRetrievalStrategy = new DepartmentRetrievalStrategy(false);
		RetrievalStrategy affiliationInDbRetrievalStrategy = new AffiliationInDbRetrievalStrategy(false);
		
		retrievalStrategies.add(emailRetrievalStrategy);
		retrievalStrategies.add(firstNameInitialRetrievalStrategy);
		retrievalStrategies.add(departmentRetrievalStrategy);
		retrievalStrategies.add(affiliationInDbRetrievalStrategy);
		
		return retrieve(retrievalStrategies, targetAuthor);
	}
	
	/**
	 * Retrieve articles.
	 * @param retrievalStrategies
	 * @param targetAuthor
	 * @return
	 */
	private List<Long> retrieve(List<RetrievalStrategy> retrievalStrategies, TargetAuthor targetAuthor) {
		String cwid = targetAuthor.getCwid();
		List<Long> pmids = new ArrayList<Long>();
		for (RetrievalStrategy retrievalStrategy : retrievalStrategies) {
			try {
				retrievalStrategy.constructPubMedQuery(targetAuthor);
				slf4jLogger.error("cwid=[" + cwid + "], pubmedQuery=[" + retrievalStrategy.getPubMedQuery() + "]");
				List<PubMedArticle> pubMedArticles = retrievalStrategy.retrieve();
				for (PubMedArticle pubMedArticle : pubMedArticles) {
					pmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
				}
				savePubMedArticles(pubMedArticles, cwid);
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
	private void savePubMedArticles(List<PubMedArticle> pubMedArticles, String cwid) {
		// Save the articles.
		pubMedService.save(pubMedArticles);

		// Save the search result.
		List<Long> pmids = new ArrayList<Long>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
		}
		eSearchResultService.pushESearchResult(new ESearchResult(cwid, pmids));
	}

	public Set<AuthorName> findUniqueAuthorsWithSameLastNameAsTargetAuthor(TargetAuthor targetAuthor) {
		Set<AuthorName> uniqueAuthors = new HashSet<AuthorName>();
		ESearchResult eSearchResult = eSearchResultService.findByCwid(targetAuthor.getCwid());
		List<Long> pmids = eSearchResult.getPmids();
		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(pmids);
		String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();

		slf4jLogger.info("number of articles=[" + pubMedArticles.size() + "].");

		for (PubMedArticle pubMedArticle : pubMedArticles) {
			if (pubMedArticle.getMedlineCitation().getArticle() != null && 
					pubMedArticle.getMedlineCitation().getArticle().getAuthorList() != null) {

				slf4jLogger.info(pubMedArticle.getMedlineCitation().getArticle().getAuthorList() + " ");

				List<MedlineCitationArticleAuthor> authors = pubMedArticle.getMedlineCitation().getArticle().getAuthorList();
				for (MedlineCitationArticleAuthor author : authors) {
					String lastName = author.getLastName();
					if (targetAuthorLastName.equals(lastName)) {
//						AuthorName authorName = getAuthorName(author);
//						uniqueAuthors.add(authorName);
					}
				}
			} else {
				slf4jLogger.info(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid() + "");
			}
		}
		return uniqueAuthors;
	}

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
