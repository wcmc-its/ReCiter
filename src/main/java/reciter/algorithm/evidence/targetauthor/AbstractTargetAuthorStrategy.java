package reciter.algorithm.evidence.targetauthor;

import org.apache.commons.lang3.StringUtils;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;

public abstract class AbstractTargetAuthorStrategy implements TargetAuthorStrategy {
	
	/**
	 * Check if the ReCiterArticle's affiliation information contains the phrase 
	 * "weill cornell", "weill-cornell", "weill medical" using case-insensitive
	 * string matching.
	 * 
	 * @param reCiterArticle
	 * @return
	 */
	protected boolean containsWeillCornell(ReCiterArticle reCiterArticle) {
		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (author.getAffiliation() != null) {
				String affiliation = author.getAffiliation().getAffiliationName();
				if (containsWeillCornell(affiliation)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean containsWeillCornell(String affiliation) {
		return 	StringUtils.containsIgnoreCase(affiliation, "weill cornell") || 
				StringUtils.containsIgnoreCase(affiliation, "weill-cornell") || 
				StringUtils.containsIgnoreCase(affiliation, "weill medical") || 
				StringUtils.containsIgnoreCase(affiliation, "cornell medical center") || 
				StringUtils.containsIgnoreCase(affiliation, "Memorial Sloan-Kettering Cancer Center") ||
				StringUtils.containsIgnoreCase(affiliation, "Sloan-Kettering") ||
				StringUtils.containsIgnoreCase(affiliation, "Sloan Kettering");
	}
}
