package reciter.model;

import java.util.ArrayList;
import java.util.List;

import reciter.model.article.ReCiterArticle;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;

public class ReCiterArticleFetcher {

	public List<ReCiterArticle> fetch(String lastName, String firstInitial, String middleName, String cwid, String email) {
		// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstInitial,null, cwid);
		
		// TODO fetch articles by email.
//		List<PubmedArticle> pubmedArticleListFromEmail = pubmedXmlFetcher.getPubmedArticleByEmail(email, cwid);
		
		// Retrieve all the scopus xml files if not exists.
		ScopusXmlFetcher scopusFetcher = new ScopusXmlFetcher();
		scopusFetcher.fetch(lastName, firstInitial, middleName, cwid);

		// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();

		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
		}
		return reCiterArticleList;
	}
}
