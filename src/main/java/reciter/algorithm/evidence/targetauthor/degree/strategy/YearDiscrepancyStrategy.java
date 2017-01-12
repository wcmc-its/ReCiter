package reciter.algorithm.evidence.targetauthor.degree.strategy;

import java.util.List;

import reciter.algorithm.evidence.article.AbstractRemoveReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

/**
 * Year-based matching for TargetAuthor (Phase II)
 * 
 * <p> Returns a year discrepancy score between an article's journal's
 * issue pub date year and a target author's terminal degree.
 * 
 * <p>Because this class extends {@code AbstractTargetAuthorStrategy}, it
 * implements the year-based matching for a target author (i.e., Phase II)
 * matching).
 * 
 * @author jil3004
 *
 */
public class YearDiscrepancyStrategy extends AbstractRemoveReCiterArticleStrategy {

	/**
	 * DegreeType used by this strategy.
	 */
	private final DegreeType degreeType;

	/**
	 * Constructor for YearDiscrepancyStrategy. Requires a DegreeType.
	 * 
	 * @param degreeType DegreeType used by this strategy.
	 */
	public YearDiscrepancyStrategy(DegreeType degreeType) {
		this.degreeType = degreeType;
	}

	/**
	 * <p>
	 * Identify year of publications for the article.
	 * Get the target author's terminal degree from rc_identity_degree.

	 * If discrepancy between pub and doctoral degree < -5, mark as false 
	 * </p>
	 * 
	 * <p>
	 * Example: 
	 * pubyear = 1990, doctoral degree = 1994, difference is -4, -5 < -4, therefore do nothing
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * pubyear = 1998, doctoral degree = 1994, difference is 4, -5 < 4, therefore do nothing.
	 * </p>
	 * 
	 * <p>
	 * If discrepancy between pub and bachelor degree < 1, mark as false
	 * Example:
	 * pubyear = 1998, bachelor degree = 1998, difference is 0, 1 < 0 is not true, therefore mark as false
	 * </p>
	 */
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		if (reCiterArticle != null 												&&
				reCiterArticle.getJournal() != null 							&& 
				reCiterArticle.getJournal().getJournalIssuePubDateYear() != 0) {

			int year = reCiterArticle.getJournal().getJournalIssuePubDateYear();

			double difference;

			if (degreeType.equals(DegreeType.BACHELORS)) {
				if (identity.getDegreeYear().getBachelorYear() != 0) {
					difference = year - identity.getDegreeYear().getBachelorYear();
					reCiterArticle.setBachelorsYearDiscrepancy(difference);
					if (difference < 1) {
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
								+ " [Bachelors Degree Difference=" + difference + "]");
						reCiterArticle.setBachelorsYearDiscrepancyScore(1);
						return 1;
					}
				}
			} else if (degreeType.equals(DegreeType.DOCTORAL)) {
				if (identity.getDegreeYear().getDoctoralYear() != 0) {
					int doctoral = identity.getDegreeYear().getDoctoralYear();
					difference = year - doctoral;
					reCiterArticle.setDoctoralYearDiscrepancy(difference);
					if (doctoral < 1998) {
						if (difference < -6) {
							reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
									+ " [Doctoral Degree Difference (<1988) =" + difference + "]");
							reCiterArticle.setDoctoralYearDiscrepancyScore(1);
							return 1;
						}
					} else {
						if (difference < -13) {
							reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
									+ " [Doctoral Degree Difference (>=1998) =" + difference + "]");
							reCiterArticle.setDoctoralYearDiscrepancyScore(1);
							return 1;
						}
					}
				}
			}
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		return 0;
	}

	public DegreeType getDegreeType() {
		return degreeType;
	}
}
