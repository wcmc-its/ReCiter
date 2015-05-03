package main.reciter.lucene.docsimilarity;

import main.reciter.lucene.DocumentVector;
import main.reciter.lucene.DocumentVectorType;
import main.reciter.model.article.ReCiterArticle;

public class MaxCosineSimilarity extends AbstractCosineSimilarity {

	private DocumentVectorType maxSimilarityType;
	
	@Override
	public double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB) {
		double max = -1;
		for (DocumentVectorType type : DocumentVectorType.values()) {
			if (type == DocumentVectorType.AFFILIATION) {
				DocumentVector docV1 = docA.getDocumentVectors().get(type);
				DocumentVector docV2 = docB.getDocumentVectors().get(type);
				double sim = cosineSim(docV1, docV2);
				if (sim > max) {
					maxSimilarityType = type;
					max = sim;
				}
			} else if (type == DocumentVectorType.KEYWORD) {
				DocumentVector docV1 = docA.getDocumentVectors().get(type);
				DocumentVector docV2 = docB.getDocumentVectors().get(type);
				double sim = cosineSim(docV1, docV2);
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
