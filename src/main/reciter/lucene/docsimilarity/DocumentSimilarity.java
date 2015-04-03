package main.reciter.lucene.docsimilarity;

import main.reciter.model.article.ReCiterArticle;

public interface DocumentSimilarity {
	double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB);
}
