package main.reciter.lucene.docvectorsimilarity;

import org.apache.commons.math3.linear.SparseRealVector;

import main.reciter.lucene.DocumentVector;

public class CosineSimilarity implements DocumentVectorSimilarity {

	@Override
	public double similarity(DocumentVector vectorA, DocumentVector vectorB) {
		double m = vectorA.getVector().getNorm() * vectorB.getVector().getNorm();
		if (m != 0) {
			return vectorA.getVector().dotProduct(vectorB.getVector()) / m;
		} else {
			return 0;
		}
	}

	@Override
	public SparseRealVector normalize(SparseRealVector sparseRealVector) {
		double sum = sparseRealVector.getL1Norm();
		sparseRealVector = (SparseRealVector) sparseRealVector.mapDivide(sum);
		return sparseRealVector;
	}

	@Override
	public String getType() {
		return getClass().getName();
	}
}
