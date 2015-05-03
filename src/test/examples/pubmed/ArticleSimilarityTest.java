package test.examples.pubmed;

import java.util.List;

import main.reciter.model.article.ReCiterArticle;
import main.xml.pubmed.PubmedXmlFetcher;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.translator.ArticleTranslator;

/**
 * Calculates the similarity between two articles. Ie: Pair-wise similarity.
 * @author jil3004
 *
 */
public class ArticleSimilarityTest {

	public static void main(String[] args) {
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedXmlFetcher.setPerformRetrievePublication(true);
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle("fernandes", "h", "hef9020");
		
		// Convert PubmedArticle to ReCiterArticle.
		List<ReCiterArticle> reCiterArticleList = ArticleTranslator.translateAll(pubmedArticleList);
		
		
		
	}
}
