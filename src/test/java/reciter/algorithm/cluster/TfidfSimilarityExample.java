package reciter.algorithm.cluster;

import java.util.ArrayList;
import java.util.List;

import reciter.tfidf.Document;
import reciter.tfidf.Term;
import reciter.tfidf.TfIdf;


public class TfidfSimilarityExample {

	public static void main(String[] args) {
		String s1 = "Athens University Medical School (Greece)";
		String s2 = "Department of Experimental Pharmacology, Athens University, School of Medicine, Athens, Greece";
		String s3 = "Weill Cornell Medical College";
		String s4 = "New York Presbyterian Hospital";

		Document doc1 = new Document(s1);
		doc1.setId(1);
		Document doc2 = new Document(s2);
		doc2.setId(2);
		Document doc3 = new Document(s3);
		doc3.setId(3);
		Document doc4 = new Document(s4);
		doc4.setId(4);
		List<Document> documents = new ArrayList<Document>();
		documents.add(doc1);
		documents.add(doc2);
		documents.add(doc3);
		documents.add(doc4);

		TfIdf tfidf = new TfIdf(documents);
		tfidf.computeTfIdf();

		for (Document document : tfidf.getDocuments()) {
			for (Term term : document.getTerms().values()) {
				System.out.println(term.getTerm() + " docId=" + document.getId() + ": score=" + term.getTfidfScore());
			}
		}
		
		double[] d1 = tfidf.createVector(doc1);
		double[] d2 = tfidf.createVector(doc2);
		double[] d3 = tfidf.createVector(doc3);
		double[] d4 = tfidf.createVector(doc4);
		
		for (double d : d1) {
			System.out.print(d + ", ");
		}
		System.out.println();
		for (double d : d2) {
			System.out.print(d + ", ");
		}
		System.out.println();
		for (double d : d3) {
			System.out.print(d + ", ");
		}
		System.out.println();
		for (double d : d4) {
			System.out.print(d + ", ");
		}
		System.out.println();
		System.out.println(tfidf.cosineSimilarity(d1, d2));
		System.out.println(tfidf.cosineSimilarity(d1, d3));
		System.out.println(tfidf.cosineSimilarity(d1, d4));		
	}
}
