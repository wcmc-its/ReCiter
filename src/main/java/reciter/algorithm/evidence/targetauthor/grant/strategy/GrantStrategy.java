package reciter.algorithm.evidence.targetauthor.grant.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleGrant;
import reciter.model.identity.Identity;

public class GrantStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		double score = 0;
		for (ReCiterArticleGrant grant : reCiterArticle.getGrantList()) {
			for (String knownGrantIds : identity.getGrants()) {
				if (grant.getGrantID() != null && grant.getGrantID().contains(knownGrantIds)) {
					reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + " [known grant ids match=" + knownGrantIds + "], ");
					score += 1;
					reCiterArticle.getMatchingGrantList().add(grant);
				}
			}
		}
		return score;
	}
	
	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double score = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			score += executeStrategy(reCiterArticle, identity);
		}
		return score;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
}
