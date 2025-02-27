package reciter.xml.retriever.pubmed;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.KnownRelationship;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

/**
 * This class retrieves all known relationships from an Identity (such as HR relationship,
 * co-investigator, manager, or mentor/mentee) and creates a query to be searched in PubMed 
 * in strict retrieval mode.
 */
@Component("knownRelationshipRetrievalStrategy")
public class KnownRelationshipRetrievalStrategy extends AbstractNameRetrievalStrategy {
	
	private static final String retrievalStrategyName = "KnownRelationshipRetrievalStrategy";
	private final static Logger slf4jLogger = LoggerFactory.getLogger(KnownRelationshipRetrievalStrategy.class);
	
	@Value("${namesIgnoredCoauthors}")
	private String excludeNames;

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	@Override
	protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity) {
	    if (identity == null) {
	        throw new IllegalArgumentException("Identity is null.");
	    }
	    if (identitynames == null || identitynames.isEmpty()) {
	        throw new IllegalArgumentException("Identity names set is null or empty.");
	    }
	    
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder(getStrategySpecificKeyword(identity))
					.author(true, false, identitynames);
		
		PubMedQuery query = pubMedQueryBuilder.build();
		slf4jLogger.info(retrievalStrategyName + " produced query=[" + query + "]");
		return query;
	}

	@Override
	protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity, Date startDate,
			Date endDate) {
	    if (identity == null) {
	        throw new IllegalArgumentException("Identity is null.");
	    }
	    if (identitynames == null || identitynames.isEmpty()) {
	        throw new IllegalArgumentException("Identity names set is null or empty.");
	    }
	    if (startDate == null || endDate == null) {
	        throw new IllegalArgumentException("Start date or end date is null.");
	    }
	    
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder(getStrategySpecificKeyword(identity))
					.author(true, false, identitynames)
					.dateRange(true, startDate, endDate);
		
		return pubMedQueryBuilder.build();
	}

	@Override
	protected String getStrategySpecificKeyword(Identity identity) {
	    if (identity == null) {
	        throw new IllegalArgumentException("Identity is null.");
	    }
		if (identity.getKnownRelationships() != null && !identity.getKnownRelationships().isEmpty()) {
			// Use the excludeNames property if provided, or default to an empty list.
			List<String> nameIgnoredAuthors = (excludeNames != null && !excludeNames.trim().isEmpty())
					? Arrays.asList(excludeNames.trim().split("\\s*,\\s*"))
					: Collections.emptyList();
			
			Iterator<KnownRelationship> iter = identity.getKnownRelationships().iterator();
			KnownRelationship firstRelationship = iter.next();
			if (firstRelationship == null || firstRelationship.getName() == null) {
			    throw new IllegalArgumentException("First known relationship or its name is null.");
			}
			String firstLastName = firstRelationship.getName().getLastName();
			String firstInitial = firstRelationship.getName().getFirstInitial();
			if (firstLastName == null || firstLastName.trim().isEmpty() ||
			    firstInitial == null || firstInitial.trim().isEmpty()) {
			    throw new IllegalArgumentException("First known relationship is missing required name fields.");
			}
			
			String firstCombined = firstLastName.trim() + " " + firstInitial.trim();
			String first = null;
			if (!nameIgnoredAuthors.contains(firstCombined)) {
				first = firstCombined + "[au]";
			}
			
			// If there are no additional relationships, return the first result.
			if (!iter.hasNext()) {
				return first;
			}
			
			// For more than one relationship, build a combined query.
			StringBuilder buf = new StringBuilder();
			if (first != null) {
				buf.append("(").append(first);
			} else {
				buf.append("(");
			}
			while (iter.hasNext()) {
				KnownRelationship knownRelationship = iter.next();
				if (knownRelationship == null || knownRelationship.getName() == null) {
				    continue; // Skip invalid entries.
				}
				String lastName = knownRelationship.getName().getLastName();
				String firstInit = knownRelationship.getName().getFirstInitial();
				if (lastName == null || lastName.trim().isEmpty() ||
				    firstInit == null || firstInit.trim().isEmpty()) {
				    continue; // Skip if required fields are missing.
				}
				String combined = lastName.trim() + " " + firstInit.trim();
				if (!nameIgnoredAuthors.contains(combined)) {
					buf.append(" OR ");
					// NOTE: The original code uses the first relationship's first initial for all entries.
					buf.append(lastName.trim() + " " + firstInitial.trim() + "[au]");
				}
			}
			buf.append(")");
			return buf.toString();
		} else {
			return null;
		}
	}
}
