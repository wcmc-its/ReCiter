package main.reciter.lucene.docsimilarity;

import main.reciter.lucene.DocumentVector;

public abstract class AbstractCosineSimilarity implements DocumentSimilarity {

	public double cosineSim(DocumentVector d1, DocumentVector d2) {

		double m = d1.getVector().getNorm() * d2.getVector().getNorm();
//		double[] a = d1.getVector().toArray();
//		double[] b = d2.getVector().toArray();
//		for (int i = 0; i < a.length; i++) {
//			if (a[i] > 0 || b[i] > 0) {
//				System.out.println(i + ": " + a[i] + " :: " + b[i]);
//			}
//		}
		if (m != 0) {
//			System.out.println("Dot prod: " + d1.getVector().dotProduct(d2.getVector()));
//			System.out.println("Score of similarity: " + m);
			return d1.getVector().dotProduct(d2.getVector()) / m;
		} else {
			return 0;
		}
	}

}
