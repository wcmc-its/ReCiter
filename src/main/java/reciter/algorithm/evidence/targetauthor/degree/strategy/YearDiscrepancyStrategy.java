package reciter.algorithm.evidence.targetauthor.degree.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class YearDiscrepancyStrategy extends AbstractTargetAuthorStrategy {

	private final DegreeType degreeType;
	
	public YearDiscrepancyStrategy(DegreeType degreeType) {
		this.degreeType = degreeType;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		if (reCiterArticle.getJournal() != null 
				&& reCiterArticle.getJournal().getJournalIssuePubDateYear() != 0
				&& targetAuthor.getDegree() != null) {
			
			int currentYearDiff = 0;
			if (degreeType.equals(DegreeType.BACHELORS)) {
				currentYearDiff = reCiterArticle.getJournal().getJournalIssuePubDateYear() - targetAuthor.getDegree().getBachelor();
			} else if (degreeType.equals(DegreeType.DOCTORAL)) {
				currentYearDiff = reCiterArticle.getJournal().getJournalIssuePubDateYear() - targetAuthor.getDegree().getDoctoral();
			} else {
				currentYearDiff = -1;
			}
			return currentYearDiff;
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return 0;
	}

	public DegreeType getDegreeType() {
		return degreeType;
	}
}
