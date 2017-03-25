package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import reciter.model.identity.Identity;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;
import reciter.xml.retriever.pubmed.PubMedQuery.PubMedQueryBuilder;

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

				// data cleaning: sometimes emails would have ',' instead of '.'
				// i.e. (ayr2001@med.cornell,edu)
				// replace ',' with '.'
				buf.append(obj.replace(',', '.'));
			}
			return buf.toString();
		} else {
			return null;
		}
	}

	@Override
	protected List<PubMedQuery> buildQuery(Identity identity) {
		List<PubMedQuery> pubMedQueries = new ArrayList<PubMedQuery>();

		PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(constructEmailQuery(identity));
		String emailQuery = pubMedQueryBuilder.build();

		PubMedQuery pubMedQuery = new PubMedQuery();
		pubMedQuery.setLenientQuery(new PubMedQueryResult(emailQuery));
		pubMedQuery.setStrictQuery(new PubMedQueryResult(emailQuery));
		pubMedQueries.add(pubMedQuery);

		return pubMedQueries;
	}

	@Override
	protected List<PubMedQuery> buildQuery(Identity identity, LocalDate startDate, LocalDate endDate) {
		List<PubMedQuery> pubMedQueries = new ArrayList<PubMedQuery>();

		PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(constructEmailQuery(identity))
				.dateRange(true, startDate, endDate);
		String emailQuery = pubMedQueryBuilder.build();

		PubMedQuery pubMedQuery = new PubMedQuery();
		pubMedQuery.setLenientQuery(new PubMedQueryResult(emailQuery));
		pubMedQuery.setStrictQuery(new PubMedQueryResult(emailQuery));
		pubMedQueries.add(pubMedQuery);

		return pubMedQueries;
	}
	
	@Override
	public RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException {
		throw new UnsupportedOperationException("Does not support retrieval by pmids.");
	}
}
