package reciter.model;

import java.util.ArrayList;
import java.util.List;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;

public class ReCiterArticleFetcher {

	public List<ReCiterArticle> fetch(TargetAuthor targetAuthor) {
		String query = targetAuthor.getPubmedSearchQuery();
		String cwid = targetAuthor.getCwid();
		
		// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(query, cwid);
		
		// Retrieve all the scopus xml files if not exists.
		ScopusXmlFetcher scopusFetcher = new ScopusXmlFetcher();
		scopusFetcher.fetch(query, cwid);

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
	
	public int checkNumQueries(TargetAuthor targetAuthor) {
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		String query = targetAuthor.getPubmedSearchQuery();
		return pubmedXmlFetcher.fetchArticleCount(query);
	}
}
