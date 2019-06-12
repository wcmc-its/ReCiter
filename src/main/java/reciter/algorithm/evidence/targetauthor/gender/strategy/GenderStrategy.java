package reciter.algorithm.evidence.targetauthor.gender.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.dynamodb.model.Gender;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.GenderEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;
import reciter.utils.GenderProbability;

@Slf4j
public class GenderStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		if(identity.getGender() != null) {
			Gender identityGender = identity.getGender();
			for (ReCiterArticle reCiterArticle : reCiterArticles) {
				Gender genderArticle = GenderProbability.getGenderArticleProbability(reCiterArticle);
				Double genderScore = null;
				if(genderArticle != null && identityGender != null) {
					genderScore = ((1 - Math.abs(identityGender.getProbability() - genderArticle.getProbability())) * ReCiterArticleScorer.strategyParameters.getGenderStrategyRangeScore() + ReCiterArticleScorer.strategyParameters.getGenderStrategyMinScore());
				}
				GenderEvidence genderEvidence = new GenderEvidence();
				if(genderArticle != null) {
					genderEvidence.setGenderScoreArticle(BigDecimal.valueOf(genderArticle.getProbability()).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
				}
				if(identityGender != null) {
					genderEvidence.setGenderScoreIdentity(BigDecimal.valueOf(identityGender.getProbability()).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
				}
				if(genderScore != null) {
					genderEvidence.setGenderScoreIdentityArticleDiscrepancy(BigDecimal.valueOf(genderScore).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
				}
				reCiterArticle.setGenderEvidence(genderEvidence);
				log.info("Pmid: " + reCiterArticle.getArticleId() + " " + genderEvidence.toString());
			}
		}
		
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		
	}

}
