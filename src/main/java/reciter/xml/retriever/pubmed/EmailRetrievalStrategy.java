package reciter.xml.retriever.pubmed;

import java.util.Iterator;

import org.springframework.stereotype.Component;

import reciter.database.mongo.model.Identity;

/**
 * There are no differences between initial query and the strict query.
 */
@Component("emailRetrievalStrategy")
public class EmailRetrievalStrategy extends AbstractRetrievalStrategy {

	private static final String retrievalStrategyName = "EmailRetrievalStrategy";
	
	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
	
	/**
	 * Concatenate email strings with " or ".
	 */
	private String constructEmailQuery(Identity identity) {
		if (identity.getEmails() != null && !identity.getEmails().isEmpty()) {
			// data cleaning: sometimes emails would have ',' instead of '.'
			// i.e. (ayr2001@med.cornell,edu)
			// replace ',' with '.'
			
			// Below is code from Apache's StringUtils class, modified to remove null checks.
			Iterator<String> iterator = identity.getEmails().iterator();
			
	        final String first = iterator.next().replace(',', '.');
	        if (!iterator.hasNext()) {
	            return first;
	        }

	        // two or more elements
	        final StringBuilder buf = new StringBuilder(30); // 30 is approx length of 2 email strings.
	        if (first != null) {
	            buf.append(first);
	        }

	        while (iterator.hasNext()) {
	            buf.append(" OR ");
	            final String obj = iterator.next();
	            buf.append(obj.replace(',', '.'));
	        }
	        return buf.toString();
		} else {
			return null;
		}
	}

	@Override
	protected String getStrategySpecificQuerySuffix(Identity identity) {
		return constructEmailQuery(identity);
	}
}
