package reciter.algorithm.feedback.article.scorer;

import java.util.List;
import java.util.Map.Entry;

import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.feedback.targetauthor.TargetAuthorFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.journal.JournalFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.journal.strategy.JournalFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.journalcategory.JournalCategoryStrategyContext;
import reciter.algorithm.evidence.targetauthor.journalcategory.strategy.JournalCategoryStrategy;
import reciter.engine.EngineParameters;
import reciter.engine.StrategyParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public class ReciterFeedbackArticleScorer extends AbstractFeedbackArticleScorer {

	private List<ReCiterArticle> reciterArticles;
	private Identity identity;
	public static StrategyParameters strategyParameters;
	/**
	 * Journal Category Score
	 */
	private StrategyContext journalStrategyContext;
	
	public ReciterFeedbackArticleScorer(List<ReCiterArticle> articles,Identity identity,EngineParameters parameters,StrategyParameters strategyParameters)
	{
		ReciterFeedbackArticleScorer.strategyParameters = strategyParameters;
		this.reciterArticles = articles;
		this.identity = identity;
		this.journalStrategyContext = new JournalFeedbackStrategyContext(new JournalFeedbackStrategy());
	}
	public void runFeedbackArticleScorer(List<ReCiterArticle> reCiterArticles, Identity identity) 
	{
		if(strategyParameters.isJournalCategory()) {
			((TargetAuthorFeedbackStrategyContext) journalStrategyContext).executeFeedbackStrategy(reCiterArticles, identity);
		}

	}
	
	

}
