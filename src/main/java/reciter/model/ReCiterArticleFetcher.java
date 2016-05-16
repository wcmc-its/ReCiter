package reciter.model;

public class ReCiterArticleFetcher {

//	public List<ReCiterArticle> fetch(TargetAuthor targetAuthor) throws ParserConfigurationException, SAXException, IOException {
//		
//		String cwid = targetAuthor.getCwid();
//
//		// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
//		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
//		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(cwid);
//
//		// Retrieve all the scopus xml files if not exists.
//		ScopusXmlFetcher scopusFetcher = new ScopusXmlFetcher();
//		scopusFetcher.fetch(cwid);
//
//		// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
//		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
//
//		for (PubmedArticle pubmedArticle : pubmedArticleList) {
//			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
//			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
//			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
//			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
//		}
//		return reCiterArticleList;
//	}
}
