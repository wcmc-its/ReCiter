package reciter.algorithm.evidence.targetauthor.journalcategory;

import org.springframework.stereotype.Component;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategy;

public class JournalCategoryStrategyContext extends AbstractTargetAuthorStrategyContext {
	
	public JournalCategoryStrategyContext(TargetAuthorStrategy strategy) {
		super(strategy);
	}
}
