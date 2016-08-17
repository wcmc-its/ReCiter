package reciter.xml.retriever.pubmed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reciter.database.mongo.model.Grant;
import reciter.database.mongo.model.Identity;

public class GrantRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	private static final String retrievalStrategyName = "GrantRetrievalStrategy";
	
	public GrantRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}
	
	@Override
	public int getThreshold() {
		return threshold;
	}

	@Override
	public void setThreshold(int threshold) {
		this.threshold = threshold;		
	}

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	@Override
	protected String constructInitialQuery(Identity identity) {
		if (identity.getGrants() != null && !identity.getGrants().isEmpty()) {
			StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Grant grant : identity.getGrants()) {
            	String sponsorAwardId = grant.getSponsorAwardId();
                String parsed = parseSponsorAwardId(sponsorAwardId);
                if (i != identity.getGrants().size() - 1) {
                    sb.append(parsed + "[Grant Number] OR ");
                } else {
                    sb.append(parsed + "[Grant Number]");
                }
            }
            String lastName = identity.getAuthorName().getLastName();
			String firstInitial = identity.getAuthorName().getFirstInitial();
			return lastName + " " + firstInitial + " AND (" + sb.toString() + ")";
		} else {
			return null;
		}
	}

	@Override
	protected String constructStrictQuery(Identity identity) {
		if (identity.getGrants() != null && !identity.getGrants().isEmpty()) {
			StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Grant grant : identity.getGrants()) {
            	String sponsorAwardId = grant.getSponsorAwardId();
                String parsed = parseSponsorAwardId(sponsorAwardId);
                if (i != identity.getGrants().size() - 1) {
                    sb.append(parsed + "[Grant Number] OR ");
                } else {
                    sb.append(parsed + "[Grant Number]");
                }
            }
            String lastName = identity.getAuthorName().getLastName();
			String firstName = identity.getAuthorName().getFirstName();
			return lastName + " " + firstName + " AND (" + sb.toString() + ")";
		} else {
			return null;
		}
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
}
