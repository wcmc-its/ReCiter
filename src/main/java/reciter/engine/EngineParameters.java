package reciter.engine;

import java.util.List;
import java.util.Map;

import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;

public class EngineParameters {

	private Identity identity;
	private List<PubMedArticle> pubMedArticles;
	private List<ScopusArticle> scopusArticles;
	private List<Long> knownPmids;
	private static Map<String, Long> meshCountMap;
	
	public Identity getIdentity() {
		return identity;
	}
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	public List<PubMedArticle> getPubMedArticles() {
		return pubMedArticles;
	}
	public void setPubMedArticles(List<PubMedArticle> pubMedArticles) {
		this.pubMedArticles = pubMedArticles;
	}
	public List<ScopusArticle> getScopusArticles() {
		return scopusArticles;
	}
	public void setScopusArticles(List<ScopusArticle> scopusArticles) {
		this.scopusArticles = scopusArticles;
	}
	public static Map<String, Long> getMeshCountMap() {
		return meshCountMap;
	}
	public static void setMeshCountMap(Map<String, Long> meshCountMap) {
		EngineParameters.meshCountMap = meshCountMap;
	}
	public List<Long> getKnownPmids() {
		return knownPmids;
	}
	public void setKnownPmids(List<Long> knownPmids) {
		this.knownPmids = knownPmids;
	}
}
