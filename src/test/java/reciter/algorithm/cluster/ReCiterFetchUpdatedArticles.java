package reciter.algorithm.cluster;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reciter.engine.ReCiterEngineProperty;
import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.xml.parser.pubmed.PubmedXmlFetcher;
import reciter.xml.parser.pubmed.model.PubmedArticle;

/**
 * Fetch additional articles when required.
 * 
 * @author Jie
 *
 */
public class ReCiterFetchUpdatedArticles {

	public static void main(String[] args) {
		ReCiterEngineProperty.loadProperty();
		
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		String cwid = "yiwang";
		TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		
		try {
			
			// First need to check email because there might be name variations.
			// Retrieve all the PubMed articles that hasn't been retrieved by email strategy.
			Set<String> pmids = pubmedXmlFetcher.fetchPmidsByEmail(targetAuthor);
			Set<String> emailPmidsAlreadyRetrieved = new HashSet<String>();
			
			List<PubmedArticle> emails = pubmedXmlFetcher.getPubmedArticle(ReCiterEngineProperty.emailXmlFolder, cwid);
			for (PubmedArticle article : emails) {
				String pmid = article.getMedlineCitation().getPmid().getPmidString();
				emailPmidsAlreadyRetrieved.add(pmid);
			}
			
			Set<String> needToBeRetrievedPmids = new HashSet<String>();
			for (String pmid : pmids) {
				if (!emailPmidsAlreadyRetrieved.contains(pmid)) {
					needToBeRetrievedPmids.add(pmid);
				}
			}
			
			// Retrieve by using PMIDS in set needToBeRetrievedPmids.
			// Write a method in pubmedXMLFetch that accepts emails.
			
			
			System.out.println(pmids);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
