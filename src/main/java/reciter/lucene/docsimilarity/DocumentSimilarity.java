package reciter.lucene.docsimilarity;

import reciter.model.article.ReCiterArticle;

public interface DocumentSimilarity {
	double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB);
}
