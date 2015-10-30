package database.dao;

import java.util.List;

import database.model.IdentityLearningRepositoryKeywords;

public interface IdentityLearningRepositoryKeywordsDao {
	public List<IdentityLearningRepositoryKeywords> getIdentityLearningRepositoryKeywordsByCwid(String cwid);
}
