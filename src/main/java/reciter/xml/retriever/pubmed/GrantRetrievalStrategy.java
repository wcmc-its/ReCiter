package reciter.xml.retriever.pubmed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import reciter.database.mongo.model.Grant;
import reciter.database.mongo.model.Identity;

@Component("grantRetrievalStrategy")
public class GrantRetrievalStrategy extends AbstractRetrievalStrategy {

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
	protected String getStrategySpecificQuerySuffix(Identity identity) {
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
}
