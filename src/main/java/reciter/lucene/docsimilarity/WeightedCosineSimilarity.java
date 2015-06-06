package reciter.lucene.docsimilarity;

import reciter.lucene.DocumentVectorType;
import reciter.model.article.ReCiterArticle;

public class WeightedCosineSimilarity extends AbstractCosineSimilarity {

	private final double AFFIL_WEIGHT = 1;
	private final double TITLE_WEIGHT = 0;
	private final double JOURN_WEIGHT = 0;
	private final double KEYWD_WEIGHT = 0;
	
	private boolean debug = false;
	@Override
	public double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB) {
		
		double affilSim = cosineSim(docA.getDocumentVectors().get(DocumentVectorType.AFFILIATION), docB.getDocumentVectors().get(DocumentVectorType.AFFILIATION));
		double titleSim = cosineSim(docA.getDocumentVectors().get(DocumentVectorType.ARTICLE_TITLE), docB.getDocumentVectors().get(DocumentVectorType.ARTICLE_TITLE));
		double journSim = cosineSim(docA.getDocumentVectors().get(DocumentVectorType.JOURNAL_TITLE), docB.getDocumentVectors().get(DocumentVectorType.JOURNAL_TITLE));
		double keyrdSim = cosineSim(docA.getDocumentVectors().get(DocumentVectorType.KEYWORD), docB.getDocumentVectors().get(DocumentVectorType.KEYWORD));
		
		if (debug) {
			System.out.println(docA.getArticleID() + " | " + docB.getArticleID() + " -- Affiliation Similarity: " + affilSim + 
					" Title Similarity: " + titleSim +
					" Journal Similarity: " + journSim +
					" Keyword Similarity: " + keyrdSim);
			
			if (affilSim > 0.5) {
				System.out.println("High Similarity: " + docA.getArticleID() + " | " + docB.getArticleID());
			}
		}
		
		return AFFIL_WEIGHT * affilSim 
			 + TITLE_WEIGHT * titleSim
			 + JOURN_WEIGHT * journSim
			 + KEYWD_WEIGHT * keyrdSim;
	}

	public double getAFFIL_WEIGHT() {
		return AFFIL_WEIGHT;
	}

	public double getTITLE_WEIGHT() {
		return TITLE_WEIGHT;
	}

	public double getJOURN_WEIGHT() {
		return JOURN_WEIGHT;
	}

	public double getKEYWD_WEIGHT() {
		return KEYWD_WEIGHT;
	}

}
