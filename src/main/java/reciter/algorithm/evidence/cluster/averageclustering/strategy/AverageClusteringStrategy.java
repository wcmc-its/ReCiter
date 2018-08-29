package reciter.algorithm.evidence.cluster.averageclustering.strategy;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.cluster.AbstractClusterStrategy;
import reciter.engine.analysis.evidence.AverageClusteringEvidence;

public class AverageClusteringStrategy extends AbstractClusterStrategy {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(AverageClusteringStrategy.class);

	@Override
	public double executeStrategy(ReCiterCluster reCiterCluster) {
		
			reCiterCluster.getArticleCluster().stream().forEach(reCiterArticle -> {
				if(ReCiterArticleScorer.strategyParameters.isUseGoldStandardEvidence()) {
					double totalArticleScoreWithoutClustering =  ((reCiterArticle.getAuthorNameEvidence() != null)?(reCiterArticle.getAuthorNameEvidence().getNameScoreTotal()):0) +
							((reCiterArticle.getEmailEvidence() != null)?reCiterArticle.getEmailEvidence().getEmailMatchScore():0) +
							reCiterArticle.getGrantEvidenceTotalScore() +
							reCiterArticle.getRelationshipEvidencesTotalScore() +
							((reCiterArticle.getEducationYearEvidence() != null)?reCiterArticle.getEducationYearEvidence().getDiscrepancyDegreeYearBachelorScore():0) +
							((reCiterArticle.getEducationYearEvidence() != null)?reCiterArticle.getEducationYearEvidence().getDiscrepancyDegreeYearDoctoralScore():0) +
							reCiterArticle.getOrganizationalEvidencesTotalScore() +
							reCiterArticle.getAffiliationScore() + 
							reCiterArticle.getArticleCountEvidence().getArticleCountScore() +
							((reCiterArticle.getPersonTypeEvidence() != null)?reCiterArticle.getPersonTypeEvidence().getPersonTypeScore():0) +
							((reCiterArticle.getJournalCategoryEvidence() != null)?reCiterArticle.getJournalCategoryEvidence().getJournalSubfieldScore():0) +
							((reCiterArticle.getAcceptedRejectedEvidence().getFeedbackScoreAccepted() !=null)?reCiterArticle.getAcceptedRejectedEvidence().getFeedbackScoreAccepted():0) + 
							((reCiterArticle.getAcceptedRejectedEvidence().getFeedbackScoreRejected() !=null)?reCiterArticle.getAcceptedRejectedEvidence().getFeedbackScoreRejected():0) +
							((reCiterArticle.getAcceptedRejectedEvidence().getFeedbackScoreNull() !=null)?reCiterArticle.getAcceptedRejectedEvidence().getFeedbackScoreNull():0);
					
					reCiterArticle.setTotalArticleScoreWithoutClustering(totalArticleScoreWithoutClustering);
				} else {
					double totalArticleScoreWithoutClustering = ((reCiterArticle.getAuthorNameEvidence() != null)?(reCiterArticle.getAuthorNameEvidence().getNameScoreTotal()):0) + 
							((reCiterArticle.getEmailEvidence() != null)?reCiterArticle.getEmailEvidence().getEmailMatchScore():0) + 
							reCiterArticle.getGrantEvidenceTotalScore() +
							reCiterArticle.getRelationshipEvidencesTotalScore() +
							((reCiterArticle.getEducationYearEvidence() != null)?reCiterArticle.getEducationYearEvidence().getDiscrepancyDegreeYearBachelorScore():0) +
							((reCiterArticle.getEducationYearEvidence() != null)?reCiterArticle.getEducationYearEvidence().getDiscrepancyDegreeYearDoctoralScore():0) +
							reCiterArticle.getOrganizationalEvidencesTotalScore() +
							reCiterArticle.getAffiliationScore() +
							reCiterArticle.getArticleCountEvidence().getArticleCountScore() +
							((reCiterArticle.getJournalCategoryEvidence() != null)?reCiterArticle.getJournalCategoryEvidence().getJournalSubfieldScore():0) +
							((reCiterArticle.getPersonTypeEvidence() != null)?reCiterArticle.getPersonTypeEvidence().getPersonTypeScore():0);
					
					reCiterArticle.setTotalArticleScoreWithoutClustering(totalArticleScoreWithoutClustering);
				}
						
			});
		
		double averageClusterScore = getAverageClusterScore(reCiterCluster);
		populateAverageClusterEvidence(reCiterCluster, averageClusterScore);
		
		
		return 0;
	}
	
	private double getAverageClusterScore(ReCiterCluster reCiterCluster) {
		double totalClusterScore = reCiterCluster.getArticleCluster().stream().mapToDouble(reCiterArticle -> reCiterArticle.getTotalArticleScoreWithoutClustering()).sum();
		return totalClusterScore/reCiterCluster.getArticleCluster().size();
	}
	
	private void populateAverageClusterEvidence(ReCiterCluster reCiterCluster, double averageClusterScore) {
		reCiterCluster.getArticleCluster().stream().forEach(reCiterArticle -> {
			double clusterScoreDiscrepancy = (reCiterArticle.getTotalArticleScoreWithoutClustering() - averageClusterScore) * ReCiterArticleScorer.strategyParameters.getClusterScoreFactor();
			AverageClusteringEvidence averageClusteringEvidence = new AverageClusteringEvidence();
			averageClusteringEvidence.setClusterScoreAverage(roundAvoid(averageClusterScore, 2));
			averageClusteringEvidence.setClusterScoreModificationOfTotalScore(roundAvoid(-clusterScoreDiscrepancy, 2));
			averageClusteringEvidence.setTotalArticleScoreWithoutClustering(roundAvoid(reCiterArticle.getTotalArticleScoreWithoutClustering(), 2));
			reCiterArticle.setTotalArticleScoreNonStandardized(roundAvoid(reCiterArticle.getTotalArticleScoreWithoutClustering() - clusterScoreDiscrepancy, 2));
			reCiterArticle.setAverageClusteringEvidence(averageClusteringEvidence);
			slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + averageClusteringEvidence);
		});
	}
	
	public static double roundAvoid(double value, int places) {
	    double scale = Math.pow(10, places);
	    return Math.round(value * scale) / scale;
	}

}
