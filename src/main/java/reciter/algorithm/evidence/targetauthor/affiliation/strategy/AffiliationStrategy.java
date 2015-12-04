package reciter.algorithm.evidence.targetauthor.affiliation.strategy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import database.dao.IdentityEarliestStartDateDao;
import database.dao.impl.IdentityEarliestStartDateDaoImpl;
import database.model.IdentityEarliestStartDate;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class AffiliationStrategy extends AbstractTargetAuthorStrategy {

	// what issue is this?
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
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
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}
}
