package reciter.algorithm.evidence.targetauthor.citizenship.strategy;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import xmlparser.scopus.model.Author;
import xmlparser.scopus.model.ScopusArticle;

public class CitizenshipStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		boolean isCitizenshipMatchFromScopus = false;
		if (reCiterArticle.getScopusArticle() != null) {
			isCitizenshipMatchFromScopus = isCitizenshipFromScopus(reCiterArticle.getScopusArticle(), targetAuthor);
		}
		boolean isCitizenShipMatchFromPubmed = false;
		boolean isFirstNameMatch = false;
		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (StringUtils.equalsIgnoreCase(author.getAuthorName().getFirstName(), targetAuthor.getAuthorName().getFirstName())) {
				isFirstNameMatch = true;
				
				if (author.getAffiliation() != null) {
					String affiliation = author.getAffiliation().getAffiliationName();
					isCitizenShipMatchFromPubmed = isCitizenshipFromPubmed(affiliation, targetAuthor);
				}
			}
		}
		if (isCitizenShipMatchFromPubmed || isCitizenshipMatchFromScopus) {
			return 1;
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Use citizenship to improve recall.
	 * (https://github.com/wcmc-its/ReCiter/issues/78).
	 */
	private boolean isCitizenshipFromPubmed(String affiliation, TargetAuthor targetAuthor) {
		if (targetAuthor.getCitizenship() != null) {
			return StringUtils.containsIgnoreCase(affiliation, targetAuthor.getCitizenship());
		}
		return false;
	}

	private boolean isCitizenshipFromScopus(ScopusArticle scopusArticle, TargetAuthor targetAuthor) {

		if (scopusArticle != null) {
			for (Entry<Long, Author> entry : scopusArticle.getAuthors().entrySet()) {
				boolean isNameMatch = entry.getValue().getSurname().equals(targetAuthor.getAuthorName().getLastName());
				if (isNameMatch) {
					if (scopusArticle.getAffiliationMap() != null && scopusArticle.getAffiliationMap().get(entry.getKey()) != null &&
							scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCountry() != null) {
						String scopusCountry = scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCountry();
						if (StringUtils.containsIgnoreCase(scopusCountry, targetAuthor.getCitizenship())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
