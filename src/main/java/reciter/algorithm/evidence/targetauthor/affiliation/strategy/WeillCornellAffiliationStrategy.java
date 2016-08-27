package reciter.algorithm.evidence.targetauthor.affiliation.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;

public class WeillCornellAffiliationStrategy extends AbstractTargetAuthorStrategy {

	private String variantName;
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		double score = 0;
		if (containsWeillCornell(reCiterArticle)) {
			reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[contains weill cornell and its variant:" + variantName + "]");
			score = 1;
		}
		reCiterArticle.setAffiliationScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, identity);
		}
		return sum;
	}
	
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
					variantName = affiliation;
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
