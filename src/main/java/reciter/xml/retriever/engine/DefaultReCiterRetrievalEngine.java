package reciter.xml.retriever.engine;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;
import reciter.xml.retriever.pubmed.EmailRetrievalStrategy;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

@Component("defaultReCiterRetrievalEngine")
public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {
	
	private final static Logger slf4jLogger = LoggerFactory.getLogger(DefaultReCiterRetrievalEngine.class);
	
	@Autowired
	private PubMedService pubMedService;
	
	@Override
	public List<PubMedArticle> retrieve(TargetAuthor targetAuthor) throws IOException {
		
		// Get all the full author names that matches that of target author.
		List<PubMedArticle> pubMedArticles = new ArrayList<PubMedArticle>();
		RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy();
		emailRetrievalStrategy.constructPubMedQuery(targetAuthor);
		
		slf4jLogger.info(emailRetrievalStrategy.getPubMedQuery());
		pubMedArticles.addAll(emailRetrievalStrategy.retrieve());
		
		
		
		
		// First retrieve using first name initial and last name. i.e., Kukafka r[au].
//		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy();
//		firstNameInitialRetrievalStrategy.constructPubMedQuery(targetAuthor);
//		
//		pubMedArticles.addAll(firstNameInitialRetrievalStrategy.retrieve());
		
		return pubMedArticles;
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
