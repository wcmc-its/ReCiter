package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.PubMedAlias;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;

public abstract class AbstractNameRetrievalStrategy extends AbstractRetrievalStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractNameRetrievalStrategy.class);

	protected abstract String buildNameQuery(String lastName, String firstName, Identity identity);
	protected abstract String buildNameQuery(String lastName, String firstName, Identity identity, LocalDate startDate, LocalDate endDate);
	protected abstract String getStrategySpecificKeyword(Identity identity);
	
	@Override
	protected List<PubMedQuery> buildQuery(Identity identity) {
		List<PubMedQuery> pubMedQueries = new ArrayList<PubMedQuery>();

		String lastName = identity.getPrimaryName().getLastName();
		String firstName = identity.getPrimaryName().getFirstName();
		String firstInitial = identity.getPrimaryName().getFirstInitial();

		PubMedQuery pubMedQuery = new PubMedQuery();
		pubMedQuery.setLenientQuery(new PubMedQueryResult(buildNameQuery(lastName, firstInitial, identity)));
		pubMedQuery.setStrictQuery(new PubMedQueryResult(buildNameQuery(lastName, firstName, identity)));

		pubMedQueries.add(pubMedQuery);
		
		for (PubMedAlias pubMedAlias : identity.getPubMedAlias()) {

			AuthorName alias = pubMedAlias.getAuthorName();
			String aliasLastName = alias.getLastName();
			String aliasFirstInitial = alias.getFirstInitial();
			String aliasFirstName = alias.getFirstName();

			PubMedQuery aliasPubMedQuery = new PubMedQuery();
			aliasPubMedQuery.setLenientQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity)));
			aliasPubMedQuery.setStrictQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity)));
			pubMedQueries.add(aliasPubMedQuery);
		}
		
		for (PubMedQuery query : pubMedQueries) {
			slf4jLogger.info(query.toString());
		}
		return pubMedQueries;
	}

	@Override
	protected List<PubMedQuery> buildQuery(Identity identity, LocalDate startDate, LocalDate endDate) {
		List<PubMedQuery> pubMedQueries = new ArrayList<PubMedQuery>();

		String lastName = identity.getPrimaryName().getLastName();
		String firstName = identity.getPrimaryName().getFirstName();
		String firstInitial = identity.getPrimaryName().getFirstInitial();

		PubMedQuery pubMedQuery = new PubMedQuery();
		pubMedQuery.setLenientQuery(new PubMedQueryResult(buildNameQuery(lastName, firstInitial, identity, startDate, endDate)));
		pubMedQuery.setStrictQuery(new PubMedQueryResult(buildNameQuery(lastName, firstName, identity, startDate, endDate)));

		pubMedQueries.add(pubMedQuery);
		
		for (PubMedAlias pubMedAlias : identity.getPubMedAlias()) {

			AuthorName alias = pubMedAlias.getAuthorName();
			String aliasLastName = alias.getLastName();
			String aliasFirstInitial = alias.getFirstInitial();
			String aliasFirstName = alias.getFirstName();

			PubMedQuery aliasPubMedQuery = new PubMedQuery();
			aliasPubMedQuery.setLenientQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity, startDate, endDate)));
			aliasPubMedQuery.setStrictQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity, startDate, endDate)));
			pubMedQueries.add(aliasPubMedQuery);
		}

		return pubMedQueries;
	}
	
	@Override
	public RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException {
		throw new UnsupportedOperationException("Does not support retrieval by pmids.");
	}
}
