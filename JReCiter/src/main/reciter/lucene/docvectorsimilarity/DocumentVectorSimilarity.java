package main.reciter.lucene.docvectorsimilarity;

import org.apache.commons.math3.linear.SparseRealVector;

import main.reciter.lucene.DocumentVector;

public interface DocumentVectorSimilarity {
	
	SparseRealVector normalize(SparseRealVector sparseRealVector);
	double similarity(DocumentVector vectorA, DocumentVector vectorB);
	String getType();
}
