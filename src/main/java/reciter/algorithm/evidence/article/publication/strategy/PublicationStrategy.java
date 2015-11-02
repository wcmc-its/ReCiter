package reciter.algorithm.evidence.article.publication.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class PublicationStrategy extends AbstractTargetAuthorStrategy{

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		//  Discount matches when a publication is not in English #103
		double score=0;
		int matches=0;
		if(reCiterArticles!=null){
			for(ReCiterArticle article: reCiterArticles){
				if(article.getArticleTitle()!=null && article.getArticleTitle().trim().startsWith("["))matches+=1;
			}
			int y=reCiterArticles.size()-matches;
			if(y>0){
				score=y/reCiterArticles.size();
			}
		}
		return score;
	}

}
