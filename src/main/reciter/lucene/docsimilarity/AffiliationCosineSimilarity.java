package main.reciter.lucene.docsimilarity;

import main.reciter.lucene.DocumentVector;
import main.reciter.lucene.DocumentVectorType;
import main.reciter.model.article.ReCiterArticle;

public class AffiliationCosineSimilarity extends AbstractCosineSimilarity {

	@Override
	public double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB) {
		double max = -1;

		DocumentVector docV1 = docA.getDocumentVectors().get(DocumentVectorType.AFFILIATION);
		DocumentVector docV2 = docB.getDocumentVectors().get(DocumentVectorType.AFFILIATION);
		double sim = cosineSim(docV1, docV2);
		if (sim > max) {
			max = sim;
		}
		System.out.println(max);
		return max;
	}
}
