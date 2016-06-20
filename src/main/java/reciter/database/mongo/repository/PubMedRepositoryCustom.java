package reciter.database.mongo.repository;

import java.util.List;

import reciter.model.pubmed.PubMedArticle;

public interface PubMedRepositoryCustom {
	List<PubMedArticle> findMatchingAuthorsByLastName(String lastName);
}
