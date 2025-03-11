package reciter.algorithm.evidence.author.feedback.authorcount.strategy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.engine.StrategyParameters;
import reciter.engine.analysis.evidence.AuthorCountEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.identity.Identity;



public class AuthorCountFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(AuthorCountFeedbackStrategy.class);
	
	public static StrategyParameters strategyParameters;
	
	public AuthorCountFeedbackStrategy(StrategyParameters strategyParameters) {
		this.strategyParameters = strategyParameters;
	}
	
	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}
	
	
	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		StopWatch stopWatchforCitesFeedback = new StopWatch("AuthorsCount");
		stopWatchforCitesFeedback.start("AuthorsCount");
		reCiterArticles.stream().forEach(reCiterArticle -> {
			
			int authorCount = reCiterArticle.getArticleCoAuthors().getAuthors().size();
	  		AuthorCountEvidence authorCountEvidence = new AuthorCountEvidence();
			double adjustedScore = calculateAdjustedArticleCountScore.apply(authorCount);
			authorCountEvidence.setCountAuthors(authorCount);
			authorCountEvidence.setAuthorCountScore(adjustedScore);
			reCiterArticle.setAuthorCountEvidence(authorCountEvidence);
			
	});
		stopWatchforCitesFeedback.stop();
		slf4jLogger.info(stopWatchforCitesFeedback.getId() + " took "
				+ stopWatchforCitesFeedback.getTotalTimeSeconds() + "s"); 
		return 0;
	}
	
	 // Function to calculate likelihood adjustment
    private static Function<Integer, Double> calculateLikelihoodAdjustment = authorCount -> {
        // Baseline likelihood (at authorCountThreshold)
    	
        double y_baseline = strategyParameters.getInCoefficent() * Math.log(strategyParameters.getAuthorCountThreshold()) + strategyParameters.getConstantCoefficeint();

        // Likelihood for the given author count
        double y = authorCount > 0 ? strategyParameters.getInCoefficent() * Math.log(authorCount) + strategyParameters.getConstantCoefficeint() : y_baseline;

        // Adjustment is scaled by gamma
        return strategyParameters.getAuthorCountAdjustmentGamma() * (y - y_baseline);
    };

    // Function to calculate adjusted article count score
    private static Function<Integer, Double> calculateAdjustedArticleCountScore = authorCount -> {
        // Apply the likelihood adjustment function
        return calculateLikelihoodAdjustment.apply(authorCount);
    };
}
