package reciter.xml.retriever.pubmed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import reciter.database.mongo.model.Identity;
import reciter.database.mongo.model.PubMedAlias;
import reciter.model.author.AuthorName;

public abstract class AbstractNameRetrievalStrategy extends AbstractRetrievalStrategy {

	protected abstract String buildNameQuery(String lastName, String firstName, Identity identity);
	protected abstract String buildNameQuery(String lastName, String firstName, Identity identity, LocalDate startDate, LocalDate endDate);
	protected abstract String getStrategySpecificKeyword(Identity identity);
	
	@Override
	protected List<PubMedQuery> buildQuery(Identity identity) {
		List<PubMedQuery> pubMedQueries = new ArrayList<PubMedQuery>();

		String lastName = identity.getAuthorName().getLastName();
		String firstName = identity.getAuthorName().getFirstName();
		String firstInitial = identity.getAuthorName().getFirstInitial();

		PubMedQuery pubMedQuery = new PubMedQuery();
		pubMedQuery.setLenientQuery(new PubMedQueryResult(buildNameQuery(lastName, firstInitial, identity)));
		pubMedQuery.setStrictQuery(new PubMedQueryResult(buildNameQuery(lastName, firstName, identity)));

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

		return pubMedQueries;
	}

	@Override
	protected List<PubMedQuery> buildQuery(Identity identity, LocalDate startDate, LocalDate endDate) {
		List<PubMedQuery> pubMedQueries = new ArrayList<PubMedQuery>();

		String lastName = identity.getAuthorName().getLastName();
		String firstName = identity.getAuthorName().getFirstName();
		String firstInitial = identity.getAuthorName().getFirstInitial();

		PubMedQuery pubMedQuery = new PubMedQuery();
		pubMedQuery.setLenientQuery(new PubMedQueryResult(buildNameQuery(lastName, firstInitial, identity, startDate, endDate)));
		pubMedQuery.setStrictQuery(new PubMedQueryResult(buildNameQuery(lastName, firstName, identity, startDate, endDate)));

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
}
