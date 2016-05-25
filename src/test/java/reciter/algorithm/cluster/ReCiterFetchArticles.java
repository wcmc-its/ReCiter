package reciter.algorithm.cluster;

import reciter.engine.ReCiterEngineProperty;

public class ReCiterFetchArticles {

	public static void main(String[] args) {
		ReCiterEngineProperty.loadProperty();

//		ReCiterEngineProperty p = new ReCiterEngineProperty();
//		for (String cwid : p.getCwids()) {
//			if (cwid.equals("mlg2007")) {
//				PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
//				TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
//				TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
//				try {
//					// Fetch by email to search for name variations.
//					pubmedXmlFetcher.fetchByEmail(targetAuthor);
//					boolean hasFetched = pubmedXmlFetcher.fetchRegular(targetAuthor);
//
//					if (!hasFetched) {
//						pubmedXmlFetcher.fetchUsingFirstName(targetAuthor);
//						pubmedXmlFetcher.fetchByAffiliationInDb(targetAuthor);
//						pubmedXmlFetcher.fetchByCommonAffiliations(targetAuthor);
//						pubmedXmlFetcher.fetchByDepartment(targetAuthor);
//						pubmedXmlFetcher.fetchByGrants(targetAuthor);
//					}
//
//					ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
//					scopusXmlFetcher.fetch(targetAuthor.getCwid());
//				} catch (IOException | SAXException | ParserConfigurationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
	}
}
