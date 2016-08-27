package reciter.algorithm.evidence.targetauthor.email.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;

public class EmailStringMatchStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		double score = 0;
		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (author.getAffiliation() != null && author.getAffiliation().getAffiliationName() != null) {
				String affiliation = author.getAffiliation().getAffiliationName();
				String emailCase1 = identity.getCwid() + "@med.cornell.edu";
				String emailCase2 = identity.getCwid() + "@mail.med.cornell.edu";
				String emailCase3 = identity.getCwid() + "@weill.cornell.edu";
				String emailCase4 = identity.getCwid() + "@nyp.org";

				if (affiliation.contains(emailCase1) ||
						affiliation.contains(emailCase2) ||
						affiliation.contains(emailCase3) ||
						affiliation.contains(emailCase4)) {

					reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + " [email matches: " + affiliation + "]");
					score += 1;
				}

				for (String email : identity.getEmails()) {
					if (affiliation.contains(email)) {
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + " [email matches: " + email + "]");
						score += 1;
					}
				}
			}
		}
		reCiterArticle.setEmailStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sumScore = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sumScore += executeStrategy(reCiterArticle, identity);
		}
		return sumScore;
	}
}
