package reciter.algorithm.evidence.targetauthor.scopus.strategy;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.xml.parser.scopus.model.Affiliation;
import reciter.xml.parser.scopus.model.Author;
import reciter.xml.parser.scopus.model.ScopusArticle;

public class StringMatchingAffiliation extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		
		double score = 0;
		ScopusArticle scopusArticle = reCiterArticle.getScopusArticle();

		if (scopusArticle != null) {
			boolean containsWeillCornellFromScopus = containsWeillCornellFromScopus(scopusArticle, targetAuthor);

			if (containsWeillCornellFromScopus) {
				for (ReCiterAuthor reCiterAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {

					boolean isFirstNameMatch = StringUtils.equalsIgnoreCase(
							reCiterAuthor.getAuthorName().getFirstInitial(), targetAuthor.getAuthorName().getFirstInitial());

					if (isFirstNameMatch) {
						score += 1;
					}
				}
			}
		}
		reCiterArticle.setScopusStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		int sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, targetAuthor);
		}
		return sum;
	}

	/**
	 * Check affiliation exists in Scopus Article.
	 * @param scopusArticle
	 * @param targetAuthor
	 * @return
	 */
	public boolean containsWeillCornellFromScopus(ScopusArticle scopusArticle, TargetAuthor targetAuthor) {
		if (scopusArticle != null) {
			for (Author scopusAuthor : scopusArticle.getAuthors().values()) {
				if (StringUtils.equalsIgnoreCase(scopusAuthor.getSurname(), targetAuthor.getAuthorName().getLastName())) {
					Set<Integer> afidSet = scopusAuthor.getAfidSet();
					for (int afid : afidSet) {
						Affiliation scopusAffialition = scopusArticle.getAffiliationMap().get(afid);
						if (scopusAffialition != null) {
							String affilName = scopusAffialition.getAffilname();
							if (containsWeillCornell(affilName)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
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
