package reciter.algorithm.evidence.targetauthor.citizenship.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.mongo.model.Feature;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.xml.parser.scopus.model.ScopusArticle;

public class CitizenshipStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		double score = 0;
		boolean isCitizenshipMatchFromScopus = false;
		if (reCiterArticle.getScopusArticle() != null) {
			isCitizenshipMatchFromScopus = isCitizenshipFromScopus(reCiterArticle.getScopusArticle(), identity);
		}
		boolean isCitizenShipMatchFromPubmed = false;
		boolean isFirstNameMatch = false;
		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (StringUtils.equalsIgnoreCase(author.getAuthorName().getFirstName(), identity.getAuthorName().getFirstName())) {
				isFirstNameMatch = true;
				
				if (author.getAffiliation() != null) {
					String affiliation = author.getAffiliation().getAffiliationName();
					isCitizenShipMatchFromPubmed = isCitizenshipFromPubmed(affiliation, identity);
				}
			}
		}
		if (isFirstNameMatch && (isCitizenShipMatchFromPubmed || isCitizenshipMatchFromScopus)) {
			score = 1;
		}
		reCiterArticle.setCitizenshipStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, identity);
		}
		return sum;
	}

	/**
	 * Use citizenship to improve recall.
	 * (https://github.com/wcmc-its/ReCiter/issues/78).
	 */
	private boolean isCitizenshipFromPubmed(String affiliation, Identity identity) {
		if (identity.getCitizenship() != null) {
			return StringUtils.containsIgnoreCase(affiliation, identity.getCitizenship());
		}
		return false;
	}

	private boolean isCitizenshipFromScopus(ScopusArticle scopusArticle, Identity identity) {

//		if (scopusArticle != null) {
//			for (Entry<Long, Author> entry : scopusArticle.getAuthors().entrySet()) {
//				boolean isNameMatch = entry.getValue().getSurname().equals(targetAuthor.getAuthorName().getLastName());
//				if (isNameMatch) {
//					if (scopusArticle.getAffiliationMap() != null && scopusArticle.getAffiliationMap().get(entry.getKey()) != null &&
//							scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCountry() != null) {
//						String scopusCountry = scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCountry();
//						if (StringUtils.containsIgnoreCase(scopusCountry, targetAuthor.getCitizenship())) {
//							return true;
//						}
//					}
//				}
//			}
//		}
		return false;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}

}
