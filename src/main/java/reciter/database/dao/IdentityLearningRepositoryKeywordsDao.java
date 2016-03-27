package reciter.database.dao;

import java.util.List;

import reciter.database.model.IdentityLearningRepositoryKeywords;

public interface IdentityLearningRepositoryKeywordsDao {
	public List<IdentityLearningRepositoryKeywords> getIdentityLearningRepositoryKeywordsByCwid(String cwid);
}
