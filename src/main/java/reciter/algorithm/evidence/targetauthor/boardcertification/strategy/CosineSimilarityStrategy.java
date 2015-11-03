package reciter.algorithm.evidence.targetauthor.boardcertification.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.model.boardcertifications.ReadBoardCertifications;


public class CosineSimilarityStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		// Leverage data on board certifications to improve phase two matching #45 
		//List<String> boardCertifications = targetAuthor.getBoardCertifications();
		ReadBoardCertifications certifications = new ReadBoardCertifications(targetAuthor.getCwid());
		targetAuthor.setBoardCertifications(certifications.getBoardCertifications());		
		return certifications.getBoardCertificationScoreByClusterArticle(reCiterArticle);
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		
		return 0;
	}
}
