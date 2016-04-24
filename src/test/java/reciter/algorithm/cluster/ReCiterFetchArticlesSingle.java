package reciter.algorithm.cluster;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import reciter.engine.ReCiterEngineProperty;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.xml.parser.pubmed.PubmedXmlFetcher;
import reciter.xml.parser.scopus.ScopusXmlFetcher;

public class ReCiterFetchArticlesSingle {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(PubmedXmlFetcher.class);
	
	public static void main(String[] args) {
		ReCiterEngineProperty.loadProperty();

		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor("aas2003");
		try {
			// Fetch by email to search for name variations.
			pubmedXmlFetcher.fetchByEmail(targetAuthor);
			
			Set<AuthorName> nameVariations = pubmedXmlFetcher.getAuthorNameVariationFromEmails(targetAuthor);
			slf4jLogger.info("Name Variations=[" + nameVariations + "]");
			
			boolean hasFetched = pubmedXmlFetcher.fetchRegular(targetAuthor);

			if (!hasFetched) {
				pubmedXmlFetcher.fetchUsingFirstName(targetAuthor);
				pubmedXmlFetcher.fetchByAffiliationInDb(targetAuthor);
				pubmedXmlFetcher.fetchByCommonAffiliations(targetAuthor);
				pubmedXmlFetcher.fetchByDepartment(targetAuthor);
				pubmedXmlFetcher.fetchByGrants(targetAuthor);
			}

//			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
//			scopusXmlFetcher.fetch(targetAuthor.getCwid());
		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
