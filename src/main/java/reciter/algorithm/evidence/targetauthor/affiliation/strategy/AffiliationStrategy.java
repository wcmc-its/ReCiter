package reciter.algorithm.evidence.targetauthor.affiliation.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.mongo.model.Identity;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;

public class AffiliationStrategy extends AbstractTargetAuthorStrategy {

	// what issue is this?
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
//		boolean containsAffiliation = containsWeillCornell(reCiterArticle);
//		if (containsAffiliation) {
//			//  Decrease likelihood of institution match if paper was published before target author's start date #104
//			IdentityEarliestStartDateDao dao = new IdentityEarliestStartDateDaoImpl() ;
//			IdentityEarliestStartDate date = dao.getIdentityEarliestStartDateByCwid(targetAuthor.getCwid()); 
//			if(date!=null){
//				DateFormat format = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
//				Date startDate;
//				try {
//					startDate = format.parse(date.getStartDate());
//					if(reCiterArticle.getJournal().getJournalIssuePubDateYear()>=startDate.getYear())return 1;
//					else return 0;
//				} catch (ParseException e) {					
//					e.printStackTrace();
//					return 1;
//				}
//			}else return 1;
//		} else {
//			return 0;
//		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
}
