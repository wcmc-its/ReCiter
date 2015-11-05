package reciter.algorithm.evidence.targetauthor.department.strategy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class DepartmentStringMatchStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		if (targetAuthor == null) {
			throw new IllegalArgumentException("Target author is null.");
		}
		
		if (reCiterArticle == null) {
			throw new IllegalArgumentException("ReCiter article is null.");
		}
		
		double score = 0;
		if (reCiterArticle.getArticleCoAuthors() != null &&
			reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				boolean isDepartmentMatch = departmentMatch(author, targetAuthor);
				boolean isFirstNameInitialMatch = 
						author.getAuthorName().getFirstInitial().equalsIgnoreCase(
								targetAuthor.getAuthorName().getFirstInitial());
				
				if (isDepartmentMatch && isFirstNameInitialMatch) {
					score = 1;
				}
			}
		}
		reCiterArticle.setDepartmentStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Leverage departmental affiliation string matching for phase two matching.
	 * 
	 * If reCiterAuthor has department information, extract the "department of ***" string and use string comparison
	 * to match to target author's primary department and other department fields. If both party's department match,
	 * return true, else return false.
	 * 
	 * (Github issue: https://github.com/wcmc-its/ReCiter/issues/79)
	 * @return True if the department of the ReCiterAuthor and TargetAuthor match.
	 */
	private boolean departmentMatch(ReCiterAuthor reCiterAuthor, TargetAuthor targetAuthor) {

		if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation().getAffiliationName() != null) {
			String affiliation = reCiterAuthor.getAffiliation().getAffiliationName();
			String extractedDept = extractDepartment(affiliation);
			String targetAuthorDept = targetAuthor.getDepartment();
			String targetAuthorOtherDept = targetAuthor.getOtherDeparment();
			if (extractedDept.equalsIgnoreCase(targetAuthorDept) || extractedDept.equalsIgnoreCase(targetAuthorOtherDept)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Extract Department information from string of the form "Department of *," or "Department of *.".
	 * 
	 * @param department Department string
	 * @return Department name.
	 */
	private String extractDepartment(String department) {
		final Pattern pattern = Pattern.compile("Department of (.+?)[\\.,]");
		final Matcher matcher = pattern.matcher(department);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}
}
