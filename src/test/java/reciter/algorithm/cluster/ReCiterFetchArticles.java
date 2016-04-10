package reciter.algorithm.cluster;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reciter.engine.ReCiterEngineProperty;
import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.xml.parser.pubmed.PubmedXmlFetcher;
import reciter.xml.parser.scopus.ScopusXmlFetcher;

public class ReCiterFetchArticles {

	public static void main(String[] args) {
		ReCiterEngineProperty.loadProperty();

		ReCiterEngineProperty p = new ReCiterEngineProperty();
		for (String cwid : p.getCwids()) {
			PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
			TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
			TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
			try {
				// Fetch by email to search for name variations.
				pubmedXmlFetcher.fetchByEmail(targetAuthor);
				pubmedXmlFetcher.fetchUsingFirstName(targetAuthor);

				pubmedXmlFetcher.fetchByAffiliationInDb(targetAuthor);
				pubmedXmlFetcher.fetchByCommonAffiliations(targetAuthor);
				pubmedXmlFetcher.fetchByDepartment(targetAuthor);
				pubmedXmlFetcher.fetchByGrants(targetAuthor);

				ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
				scopusXmlFetcher.fetch(targetAuthor.getCwid());
			} catch (IOException | SAXException | ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
