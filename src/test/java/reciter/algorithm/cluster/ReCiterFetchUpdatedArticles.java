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
		String cwid = "aas2003";
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
			System.out.println("Need to retrieve=[" + needToBeRetrievedPmids + "].");
			pubmedXmlFetcher.fetchByPmids(needToBeRetrievedPmids, ReCiterEngineProperty.emailXmlFolder, cwid);

			// Check missing PubMed articles for other strategies.

			// 1. for affiliations.
			Set<String> affiliationPmids = pubmedXmlFetcher.fetchPmidsByAffiliationInDb(targetAuthor);
			Set<String> affiliationPmidsAlreadyRetrieved = new HashSet<String>();

			List<PubmedArticle> affiliation = pubmedXmlFetcher.getPubmedArticle(ReCiterEngineProperty.affiliationsXmlFolder, cwid);
			for (PubmedArticle article : affiliation) {
				String pmid = article.getMedlineCitation().getPmid().getPmidString();
				affiliationPmidsAlreadyRetrieved.add(pmid);
			}

			Set<String> affiliationNeedToBeRetrievedPmids = new HashSet<String>();
			for (String pmid : affiliationPmids) {
				if (!affiliationPmidsAlreadyRetrieved.contains(pmid)) {
					affiliationNeedToBeRetrievedPmids.add(pmid);
				}
			}

			System.out.println("Need to retrieve=[" + affiliationNeedToBeRetrievedPmids + "].");
			pubmedXmlFetcher.fetchByPmids(affiliationNeedToBeRetrievedPmids, ReCiterEngineProperty.commonAffiliationsXmlFolder, cwid);

			// 2. for common affiliations.
			Set<String> commonAffiliationPmids = pubmedXmlFetcher.fetchPmidsByCommonAffiliations(targetAuthor);
			Set<String> commonAffiliationPmidsAlreadyRetrieved = new HashSet<String>();

			List<PubmedArticle> commonAffiliation = pubmedXmlFetcher.getPubmedArticle(ReCiterEngineProperty.commonAffiliationsXmlFolder, cwid);
			for (PubmedArticle article : commonAffiliation) {
				String pmid = article.getMedlineCitation().getPmid().getPmidString();
				commonAffiliationPmidsAlreadyRetrieved.add(pmid);
			}

			Set<String> commonAffiliationNeedToBeRetrievedPmids = new HashSet<String>();
			for (String pmid : commonAffiliationPmids) {
				if (!commonAffiliationPmidsAlreadyRetrieved.contains(pmid)) {
					commonAffiliationNeedToBeRetrievedPmids.add(pmid);
				}
			}

			System.out.println("Need to retrieve=[" + commonAffiliationNeedToBeRetrievedPmids + "].");
			pubmedXmlFetcher.fetchByPmids(commonAffiliationNeedToBeRetrievedPmids, ReCiterEngineProperty.grantXmlFolder, cwid);
			
			// 3. for grants.
			Set<String> grantPmids = pubmedXmlFetcher.fetchPmidsByGrants(targetAuthor);
			Set<String> grantPmidsAlreadyRetrieved = new HashSet<String>();

			List<PubmedArticle> grantArticles = pubmedXmlFetcher.getPubmedArticle(ReCiterEngineProperty.grantXmlFolder, cwid);
			for (PubmedArticle article : grantArticles) {
				String pmid = article.getMedlineCitation().getPmid().getPmidString();
				grantPmidsAlreadyRetrieved.add(pmid);
			}

			Set<String> grantNeedToBeRetrievedPmids = new HashSet<String>();
			for (String pmid : grantPmids) {
				if (!grantPmidsAlreadyRetrieved.contains(pmid)) {
					grantNeedToBeRetrievedPmids.add(pmid);
				}
			}

			System.out.println("Need to retrieve=[" + grantNeedToBeRetrievedPmids + "].");
			pubmedXmlFetcher.fetchByPmids(grantNeedToBeRetrievedPmids, ReCiterEngineProperty.grantXmlFolder, cwid);
			
			// 4. for departments.
			Set<String> deptPmids = pubmedXmlFetcher.fetchPmidsByDepartment(targetAuthor);
			Set<String> deptPmidsAlreadyRetrieved = new HashSet<String>();

			List<PubmedArticle> deptArticles = pubmedXmlFetcher.getPubmedArticle(ReCiterEngineProperty.departmentXmlFolder, cwid);
			for (PubmedArticle article : deptArticles) {
				String pmid = article.getMedlineCitation().getPmid().getPmidString();
				deptPmidsAlreadyRetrieved.add(pmid);
			}

			Set<String> deptNeedToBeRetrievedPmids = new HashSet<String>();
			for (String pmid : deptPmids) {
				if (!deptPmidsAlreadyRetrieved.contains(pmid)) {
					deptNeedToBeRetrievedPmids.add(pmid);
				}
			}

			System.out.println("Need to retrieve=[" + deptNeedToBeRetrievedPmids + "].");
			pubmedXmlFetcher.fetchByPmids(deptNeedToBeRetrievedPmids, ReCiterEngineProperty.departmentXmlFolder, cwid);

		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
