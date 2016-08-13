package reciter.algorithm.evidence.targetauthor.department.strategy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

/**
 * 
 * @author jil3004
 *
 */
public class DepartmentStringMatchStrategy extends AbstractTargetAuthorStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(DepartmentStringMatchStrategy.class);

	private String extractedDept;
	private long pmid;
	private int isGoldStandard;
//	private Set<String> departments = new HashSet<String>();
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {

		pmid = reCiterArticle.getArticleId();
		isGoldStandard = reCiterArticle.getGoldStandard();

		double score = 0;
		if (reCiterArticle.getArticleCoAuthors() != null && reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {

//				boolean isDepartmentMatch = departmentMatchStrict(author, targetAuthor);
				boolean isDepartmentMatch = departmentMatchStrictAndFillInAffiliationIfNotPresent(
						reCiterArticle.getArticleCoAuthors().getAuthors(), author, targetAuthor);
				
				boolean isFirstNameInitialMatch = 
						author.getAuthorName().getFirstInitial().equalsIgnoreCase(targetAuthor.getAuthorName().getFirstInitial());

				boolean isFirstNameInitialMatchFromEmailFetched = false;
				if (targetAuthor.getAuthorNamesFromEmailFetch() != null) {
					for (AuthorName authorName : targetAuthor.getAuthorNamesFromEmailFetch()) {
						if (StringUtils.equalsIgnoreCase(authorName.getFirstInitial(), author.getAuthorName().getFirstInitial()) &&
								StringUtils.equalsIgnoreCase(authorName.getLastName(), author.getAuthorName().getLastName())) {
							isFirstNameInitialMatchFromEmailFetched = true;
							break;
						}
					}
				}

				if ((isDepartmentMatch && isFirstNameInitialMatch) || (isDepartmentMatch && isFirstNameInitialMatchFromEmailFetched)) {
					reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
							" [department and first name initial matches: " + extractedDept + 
							", first name initial: " + targetAuthor.getAuthorName().getFirstInitial() + "]");
					slf4jLogger.info("Department and first name initial matches. "
							+ "PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
							"] Is Gold=[" + isGoldStandard + "]");
					score = 1;
					break;
				}
			}
		}
		reCiterArticle.setDepartmentStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, targetAuthor);
		}
		return sum;
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
			String targetAuthorOtherDept = targetAuthor.getOtherDepartment();
			if (StringUtils.containsIgnoreCase(extractedDept, targetAuthorDept) || 
					StringUtils.containsIgnoreCase(extractedDept, targetAuthorOtherDept) ||
					StringUtils.containsIgnoreCase(targetAuthorDept, extractedDept) || 
					StringUtils.containsIgnoreCase(targetAuthorOtherDept, extractedDept)) {

				return true;
			}
		}
		return false;
	}

	private boolean departmentMatchStrictAndFillInAffiliationIfNotPresent(List<ReCiterAuthor> authors, 
			ReCiterAuthor reCiterAuthor, TargetAuthor targetAuthor) {

		String targetAuthorDept = targetAuthor.getDepartment();
		String targetAuthorOtherDept = targetAuthor.getOtherDepartment();
		
		if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation().getAffiliationName() != null) {
			String affiliation = reCiterAuthor.getAffiliation().getAffiliationName();
			extractedDept = extractDepartment(affiliation);
			
			if (StringUtils.equalsIgnoreCase(extractedDept, targetAuthorDept) || 
				StringUtils.equalsIgnoreCase(extractedDept, targetAuthorOtherDept)) {
				return true;
			}

			if (targetAuthor.getAlternateDepartmentNames() != null) {
				for (String alternateDeptName : targetAuthor.getAlternateDepartmentNames()) {
					if (StringUtils.equalsIgnoreCase(alternateDeptName, extractedDept)) {
						slf4jLogger.info("PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
								"] Alternate Dept Name=[" + alternateDeptName + "] Is Gold=[" + isGoldStandard + "]");
						return true;
					}
				}
			}
		} else {
			// get affiliation from one of the other authors.
			for (ReCiterAuthor author : authors) {
				if (author.getAffiliation() != null && author.getAffiliation().getAffiliationName() != null 
						&& author.getAffiliation().getAffiliationName().length() > 0) {
					String affiliation = author.getAffiliation().getAffiliationName();
					extractedDept = extractDepartment(affiliation);
					
					if (StringUtils.equalsIgnoreCase(extractedDept, targetAuthorDept) || 
						StringUtils.equalsIgnoreCase(extractedDept, targetAuthorOtherDept)) {
						return true;
					}

					if (targetAuthor.getAlternateDepartmentNames() != null) {
						for (String alternateDeptName : targetAuthor.getAlternateDepartmentNames()) {
							if (StringUtils.equalsIgnoreCase(alternateDeptName, extractedDept)) {
								slf4jLogger.info("PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
										"] Alternate Dept Name=[" + alternateDeptName + "] Is Gold=[" + isGoldStandard + "]");
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private boolean departmentMatchStrict(ReCiterAuthor reCiterAuthor, TargetAuthor targetAuthor) {

		String targetAuthorDept = targetAuthor.getDepartment();
		String targetAuthorOtherDept = targetAuthor.getOtherDepartment();
		
		// this causes precision to decrease, but increases recall.
//		if (departments.contains(targetAuthorDept) || departments.contains(targetAuthorOtherDept)) {
//			return true;
//		}
		
		if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation().getAffiliationName() != null) {
			String affiliation = reCiterAuthor.getAffiliation().getAffiliationName();
			extractedDept = extractDepartment(affiliation);
//			if (extractedDept.length() > 0) {
//				departments.add(extractedDept);
//			}
			
			if (StringUtils.equalsIgnoreCase(extractedDept, targetAuthorDept) || 
				StringUtils.equalsIgnoreCase(extractedDept, targetAuthorOtherDept)) {
				return true;
			}

			if (targetAuthor.getAlternateDepartmentNames() != null) {
				for (String alternateDeptName : targetAuthor.getAlternateDepartmentNames()) {
					if (StringUtils.equalsIgnoreCase(alternateDeptName, extractedDept)) {
//						slf4jLogger.info("PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
//								"] Alternate Dept Name=[" + alternateDeptName + "] Is Gold=[" + isGoldStandard + "]");
						return true;
					}
				}
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
