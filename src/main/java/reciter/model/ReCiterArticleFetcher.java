package reciter.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.xml.parser.pubmed.PubmedXmlFetcher;
import reciter.xml.parser.pubmed.model.PubmedArticle;
import reciter.xml.parser.scopus.ScopusXmlFetcher;
import reciter.xml.parser.scopus.model.ScopusArticle;
import reciter.xml.parser.translator.ArticleTranslator;

public class ReCiterArticleFetcher {

	public List<ReCiterArticle> fetch(TargetAuthor targetAuthor) throws ParserConfigurationException, SAXException, IOException {
		
		String cwid = targetAuthor.getCwid();

		// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(cwid);

		// Retrieve all the scopus xml files if not exists.
		ScopusXmlFetcher scopusFetcher = new ScopusXmlFetcher();
		scopusFetcher.fetch(cwid);

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
