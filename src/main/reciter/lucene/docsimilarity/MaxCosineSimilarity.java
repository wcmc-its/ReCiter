package main.reciter.lucene.docsimilarity;

import main.reciter.lucene.DocumentVector;
import main.reciter.lucene.DocumentVectorType;
import main.reciter.model.article.ReCiterArticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxCosineSimilarity extends AbstractCosineSimilarity {

	private DocumentVectorType maxSimilarityType;

	private final static Logger slf4jLogger = LoggerFactory.getLogger(MaxCosineSimilarity.class);

	@Override
	public double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB) {
		double max = -1;
		for (DocumentVectorType type : DocumentVectorType.values()) {
			//			if (type != DocumentVectorEnum.PMID) {
			// Only comparing affiliation:
			if (type == DocumentVectorType.AFFILIATION) {
				DocumentVector docV1 = docA.getDocumentVectors().get(type);
				DocumentVector docV2 = docB.getDocumentVectors().get(type);
				double sim = cosineSim(docV1, docV2);
//				if (docB.getArticleID() == 15014292){
//					slf4jLogger.info("Cosine Similarity: " + docA.getArticleID() + ", " + docB.getArticleID() + ": " + sim);
//				}
//					slf4jLogger.info("Affiilation: " + docA.getAffiliationConcatenated() + " || " + docB.getAffiliationConcatenated());
//					
//					slf4jLogger.info(docV1.getTermToFreqMap().toString());
//					slf4jLogger.info(docV2.getTermToFreqMap().toString());
//					
//					slf4jLogger.info("Dimension: " + docV1.getVector().getDimension() + "");
//					double[] a = docV1.getVector().toArray();
//					for (int i = 0; i < a.length; i++) {
//						slf4jLogger.info(a[i] + "");
//					}
//				}
				if (sim > max) {
					maxSimilarityType = type;
					max = sim;
				}
			}
		}
		return max;
	}

	public DocumentVectorType getMaxSimilarityType() {
		return maxSimilarityType;
	}
}
