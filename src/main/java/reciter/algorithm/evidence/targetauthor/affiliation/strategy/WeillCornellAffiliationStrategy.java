package reciter.algorithm.evidence.targetauthor.affiliation.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.mongo.model.Identity;
import reciter.engine.Feature;
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
				List<String> affiliationsMatches = containsWeillCornell(affiliation);
				if (affiliationsMatches.size() > 0) {
					reCiterArticle.setFrequentInstitutionalCollaborators(affiliationsMatches);
					variantName = affiliation;
					return true;
				}
			}
		}
		return false;
	}
	
	protected List<String> containsWeillCornell(String affiliation) {
		List<String> affiliationsMatches = new ArrayList<String>();
		
		if (StringUtils.containsIgnoreCase(affiliation, "weill cornell")) {
			affiliationsMatches.add("weill cornell");
		}
		
		if (StringUtils.containsIgnoreCase(affiliation, "weill-cornell")) {
			affiliationsMatches.add("weill-cornell");
		}
		
		if (StringUtils.containsIgnoreCase(affiliation, "weill medical")) {
			affiliationsMatches.add("weill medical");
		}
		
		if (StringUtils.containsIgnoreCase(affiliation, "cornell medical center")) {
			affiliationsMatches.add("cornell medical center");
		}
		
		if (StringUtils.containsIgnoreCase(affiliation, "Memorial Sloan-Kettering Cancer Center")) {
			affiliationsMatches.add("Memorial Sloan-Kettering Cancer Center");
		}
		
		if (StringUtils.containsIgnoreCase(affiliation, "Sloan-Kettering")) {
			affiliationsMatches.add("Sloan-Kettering");
		}
		
		if (StringUtils.containsIgnoreCase(affiliation, "Sloan Kettering")) {
			affiliationsMatches.add("Sloan Kettering");
		}
		
		return affiliationsMatches;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		if (containsWeillCornell(reCiterArticle)) {
			feature.setWeillCornellAffiliation(1);
		}
	}
}
