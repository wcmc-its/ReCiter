package reciter.lucene.docsimilarity;

import reciter.lucene.DocumentVector;

public abstract class AbstractCosineSimilarity implements DocumentSimilarity {

	public double cosineSim(DocumentVector d1, DocumentVector d2) {

		double m = d1.getVector().getNorm() * d2.getVector().getNorm();

		if (m != 0) {
			return d1.getVector().dotProduct(d2.getVector()) / m;
		} else {
			return 0;
		}
	}

}
