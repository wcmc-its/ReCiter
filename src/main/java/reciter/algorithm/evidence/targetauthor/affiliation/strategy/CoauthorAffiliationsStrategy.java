//package reciter.algorithm.evidence.targetauthor.affiliation.strategy;
//
//import java.util.List;
//
//import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
//import reciter.database.mongo.model.Identity;
//import reciter.model.article.ReCiterArticle;
//import reciter.model.author.ReCiterAuthor;
//import reciter.service.CoauthorAffiliationsService;
//import reciter.service.bean.CoauthorAffiliationsBean;
//
///**
// * Assign Phase Two score to reflect the extent to which candidate articles 
// * have authors with affiliations that occur frequently with WCMC authors.
// * 
// * https://github.com/wcmc-its/ReCiter/issues/74.
// * @author jil3004
// *
// */
//public class CoauthorAffiliationsStrategy extends AbstractTargetAuthorStrategy {
//
//	private CoauthorAffiliationsService coauthorAffiliationsService;
//	
//	public CoauthorAffiliationsStrategy(CoauthorAffiliationsService coauthorAffiliationsService) {
//		this.coauthorAffiliationsService = coauthorAffiliationsService;
//	}
//	
//	@Override
//	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
//		double averageScore = 0;
//		if (reCiterArticle.getArticleCoAuthors() != null) {
//			List<ReCiterAuthor> authors = reCiterArticle.getArticleCoAuthors().getAuthors();
//			for (ReCiterAuthor author : authors) {
//				if (author.getAffiliation() != null) {
//					String affiliation = author.getAffiliation().getAffiliationName();
//					if (affiliation != null && affiliation.length() > 0) {
//						CoauthorAffiliationsBean coauthorAffiliations = coauthorAffiliationsService.getCoauthorAffiliationsByLabel(affiliation);
//						if (coauthorAffiliations != null && coauthorAffiliations.getScore() != 0) {
//							averageScore += coauthorAffiliations.getScore();
//						}
//					}
//				}
//			}
//		}
//		return averageScore;
//	}
//
//	@Override
//	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//}
