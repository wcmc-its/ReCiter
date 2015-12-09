package reciter.algorithm.evidence.targetauthor.name.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.model.IdentityDirectory;
import reciter.algorithm.evidence.article.AbstractRemoveReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.tfidf.ReCiterStringUtil;

public class RemoveByNameStrategy extends AbstractRemoveReCiterArticleStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(RemoveByNameStrategy.class);

	private String firstNameFieldVar;
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		boolean shouldRemove = false;
		boolean foundAuthorWithSameFirstName = false;
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String firstName = author.getAuthorName().getFirstName();
				String lastName = author.getAuthorName().getLastName();

				String targetAuthorFirstName = targetAuthor.getAuthorName().getFirstName();
				String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();

				// Check if last name matches.
				if (StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(lastName), 
						ReCiterStringUtil.deAccent(targetAuthorLastName))) {

					if (firstName != null && targetAuthorFirstName != null) {

						firstName = firstName.trim();
						targetAuthorFirstName = targetAuthorFirstName.trim();
					}
					// Check if first name is a full name (not an initial).
					if (firstName.length() > 1 && targetAuthorFirstName.length() > 1) {
						
						// First name doesn't match! Should remove the article from the selected cluster.
						if (!StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {
							
							firstNameFieldVar = firstName;
//							slf4jLogger.info("Removed article id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
//									targetAuthor.getCwid() + "] and name in article=[" + firstName + "]" +
//									" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
							shouldRemove = true;

							// Check rc_identity_directory.
							List<IdentityDirectory> aliasList = targetAuthor.getAliasList();
							if (aliasList != null) {
								for (IdentityDirectory identityDirectory : aliasList) {
									String givenName = identityDirectory.getGivenName();
									if (StringUtils.equalsIgnoreCase(givenName, firstName)) {
//										slf4jLogger.info("Un-Removed article id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
//												targetAuthor.getCwid() + "] and name in article=[" + firstName + "]" +
//												" Name in identity directory=[" + givenName + "]" +  
//												" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
										shouldRemove = false;
										break;
									}
								}
							}
							
							// If currently being, removed check Levenshtein distance.
							if (shouldRemove) {
								int levenshteinDistance = ReCiterStringUtil.levenshteinDistance(firstName, targetAuthorFirstName);
								if (levenshteinDistance <= 1) {
//									slf4jLogger.info("Un-Removed article id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
//											targetAuthor.getCwid() + "] and name in article=[" + firstName + "]" +
//											" Levenshtein Distance=[" + levenshteinDistance + "]" +  
//											" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
									shouldRemove = false;
								}
							}
							
							// Case: "Joan h f". Split the first name by white space and check whether
							// the first names match. If they do, do not remove article from selected
							// cluster.
							if (shouldRemove) {
								String[] firstNameArray = firstName.split("\\s+");
								if (firstNameArray.length > 1) {
									firstName = firstNameArray[0];
									if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {
										slf4jLogger.info("Un-Removed article (same name). id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
										targetAuthor.getCwid() + "] and name in article=[" + firstName + "]" +
										" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
										shouldRemove = false;
									}
								}
							}
						} else {
							// Handle the case where there are multiple authors with the same last name.
//							slf4jLogger.info("Un-Removed article (same name). id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
//									targetAuthor.getCwid() + "] and name in article=[" + firstName + "]" +
//									" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
							foundAuthorWithSameFirstName = true;
						}
					}
				}
			}
		}

		if (shouldRemove && !foundAuthorWithSameFirstName) {
			slf4jLogger.info("Removed article id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
					targetAuthor.getCwid() + "] and name in article=[" + firstNameFieldVar + "]" +
					" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}
}
