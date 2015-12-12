package reciter.algorithm.evidence.targetauthor.email.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class EmailStringMatchStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		double score = 0;
		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (author.getAffiliation() != null && author.getAffiliation().getAffiliationName() != null) {
				String affiliation = author.getAffiliation().getAffiliationName();
				String emailCase1 = targetAuthor.getCwid() + "@med.cornell.edu";
				String emailCase2 = targetAuthor.getCwid() + "@mail.med.cornell.edu";
				String emailCase3 = targetAuthor.getCwid() + "@weill.cornell.edu";
				String emailCase4 = targetAuthor.getCwid() + "@nyp.org";
				String targetAuthorEmailAddress = targetAuthor.getEmail();
				String targetAuthorEmailAddressOther = targetAuthor.getEmailOther();

				if (affiliation.contains(emailCase1) ||
						affiliation.contains(emailCase2) ||
						affiliation.contains(emailCase3) ||
						affiliation.contains(emailCase4)) {

					reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + " [email matches: " + affiliation + "]");
					score += 1;
				}

				if (targetAuthorEmailAddress != null) {
					if (affiliation.contains(targetAuthorEmailAddress)) {
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + " [email matches: " + targetAuthorEmailAddress + "]");
						score += 1;
					}
				}
				
				if (targetAuthorEmailAddressOther != null) {
					if (affiliation.contains(targetAuthorEmailAddressOther)) {
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + " [email matches: " + targetAuthorEmailAddressOther + "]");
						score += 1;
					}
				}
			}
		}
		reCiterArticle.setEmailStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		double sumScore = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sumScore += executeStrategy(reCiterArticle, targetAuthor);
		}
		return sumScore;
	}
}
