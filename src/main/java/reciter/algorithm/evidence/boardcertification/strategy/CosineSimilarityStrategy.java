package reciter.algorithm.evidence.boardcertification.strategy;

import java.util.List;

import reciter.algorithm.evidence.boardcertification.AbstractStrategy;
import reciter.algorithm.tfidf.Document;
import reciter.model.article.ReCiterArticle;


public class CosineSimilarityStrategy extends AbstractStrategy {

	@Override
	public double executeStrategy(List<String> boardCertifications, ReCiterArticle article) {
//		String s = preprocess(boardCertifications);
//		Document d1 = new Document(s);
//		Document d2 = new Document(article.getJournal().getJournalTitle());
		return 0;
	}
}
