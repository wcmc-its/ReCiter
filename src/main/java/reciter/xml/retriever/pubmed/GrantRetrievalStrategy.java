package reciter.xml.retriever.pubmed;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import reciter.model.identity.Grant;
import reciter.model.identity.Identity;
import reciter.xml.retriever.pubmed.PubMedQuery.PubMedQueryBuilder;

@Component("grantRetrievalStrategy")
public class GrantRetrievalStrategy extends AbstractNameRetrievalStrategy {

	private static final String retrievalStrategyName = "GrantRetrievalStrategy";

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	/**
	 * Function to parse sponsortAwardId.
	 * @param sponsorAwardId
	 * @return
	 */
	private String parseSponsorAwardId(String sponsorAwardId) {
		int lastIndexOfSpace = sponsorAwardId.lastIndexOf(" ");
		if (lastIndexOfSpace != -1) {
			String temp = sponsorAwardId.substring(lastIndexOfSpace + 1, sponsorAwardId.length());
			final Pattern pattern = Pattern.compile("([^-]*)-");
			final Matcher matcher = pattern.matcher(temp);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return "";
	}

	@Override
	protected String getStrategySpecificKeyword(Identity identity) {
		if (identity.getGrants() != null && !identity.getGrants().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (Grant grant : identity.getGrants()) {
				String sponsorAwardId = grant.getSponsorAwardId();
				if (sponsorAwardId != null) {
					String parsed = parseSponsorAwardId(sponsorAwardId);
					if (i != identity.getGrants().size() - 1) {
						sb.append(parsed + "[Grant Number] OR ");
					} else {
						sb.append(parsed + "[Grant Number]");
					}
					i++;
				}
			}
			return "(" + sb.toString() + ")";
		} else {
			return null;
		}
	}

	@Override
	protected String buildNameQuery(String lastName, String firstName, Identity identity) {
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder(getStrategySpecificKeyword(identity))
					.author(true, lastName, firstName);
		
		return pubMedQueryBuilder.build();
	}
	
	@Override
	protected String buildNameQuery(String lastName, String firstName, Identity identity, LocalDate startDate,
			LocalDate endDate) {
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder(getStrategySpecificKeyword(identity))
					.author(true, lastName, firstName)
					.dateRange(true, startDate, endDate);
		
		return pubMedQueryBuilder.build();
	}
}
