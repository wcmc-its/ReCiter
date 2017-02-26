package reciter.engine.erroranalysis;

import java.util.List;

import reciter.model.article.ReCiterArticleGrant;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.KnownRelationship;

public class ReCiterAnalysis {
	private String cwid;
	private List<ReCiterAnalysisArticle> reCiterAnalysisArticles;
	
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	
	public List<ReCiterAnalysisArticle> getReCiterAnalysisArticles() {
		return reCiterAnalysisArticles;
	}
	public void setReCiterAnalysisArticles(List<ReCiterAnalysisArticle> reCiterAnalysisArticle) {
		this.reCiterAnalysisArticles = reCiterAnalysisArticle;
	}

	public static class ReCiterAnalysisArticle {
		private long pmid;
		private Citation citation;
		
		public static class Citation {
			private int pubDate;
			private List<ReCiterAuthor> authorList;
			private Journal journal;
			
			public int getPubDate() {
				return pubDate;
			}
			public void setPubDate(int pubDate) {
				this.pubDate = pubDate;
			}
			public List<ReCiterAuthor> getAuthorList() {
				return authorList;
			}
			public void setAuthorList(List<ReCiterAuthor> authorList) {
				this.authorList = authorList;
			}
			public Journal getJournal() {
				return journal;
			}
			public void setJournal(Journal journal) {
				this.journal = journal;
			}
			public String getVolume() {
				return volume;
			}
			public void setVolume(String volume) {
				this.volume = volume;
			}
			public String getIssue() {
				return issue;
			}
			public void setIssue(String issue) {
				this.issue = issue;
			}
			public String getPages() {
				return pages;
			}
			public void setPages(String pages) {
				this.pages = pages;
			}
			public String getPmcid() {
				return pmcid;
			}
			public void setPmcid(String pmcid) {
				this.pmcid = pmcid;
			}
			public String getDoi() {
				return doi;
			}
			public void setDoi(String doi) {
				this.doi = doi;
			}
			
			public static class Journal {
				private String verbose;
				private String medlineTA;
				public String getVerbose() {
					return verbose;
				}
				public void setVerbose(String verbose) {
					this.verbose = verbose;
				}
				public String getMedlineTA() {
					return medlineTA;
				}
				public void setMedlineTA(String medlineTA) {
					this.medlineTA = medlineTA;
				}
			}
			
			private String volume;
			private String issue;
			private String pages;
			private String pmcid;
			private String doi;
		}
		
		public long getPmid() {
			return pmid;
		}

		public void setPmid(long pmid) {
			this.pmid = pmid;
		}

		public Citation getCitation() {
			return citation;
		}

		public void setCitation(Citation citation) {
			this.citation = citation;
		}

		public String getUserAssertion() {
			return userAssertion;
		}

		public void setUserAssertion(String userAssertion) {
			this.userAssertion = userAssertion;
		}

		public PositiveEvidence getPositiveEvidence() {
			return positiveEvidence;
		}

		public void setPositiveEvidence(PositiveEvidence positiveEvidence) {
			this.positiveEvidence = positiveEvidence;
		}

		private String userAssertion;
		private PositiveEvidence positiveEvidence;
		
		public static class PositiveEvidence {
			
			private ReCiterAuthor matchingNameVariant;
			private String matchingDepartment;
			private List<KnownRelationship> matchingRelationships;
			private List<String> matchingInstitutionTargetAuthors;
			private String matchingInstitutionFrequentCollaborator;
			private List<ReCiterArticleGrant> matchingGrantIDs;
			private List<String> matchingEmails;
			private String publishedPriorAcademicDegree;
			private String clusteredWithOtherMatchingArticles;
			
			public ReCiterAuthor getMatchingNameVariant() {
				return matchingNameVariant;
			}
			public void setMatchingNameVariant(ReCiterAuthor matchingNameVariant) {
				this.matchingNameVariant = matchingNameVariant;
			}
			public String getMatchingDepartment() {
				return matchingDepartment;
			}
			public void setMatchingDepartment(String matchingDepartment) {
				this.matchingDepartment = matchingDepartment;
			}
			public List<KnownRelationship> getMatchingRelationships() {
				return matchingRelationships;
			}
			public void setMatchingRelationships(List<KnownRelationship> matchingRelationship) {
				this.matchingRelationships = matchingRelationship;
			}
			public List<String> getMatchingInstitutionTargetAuthors() {
				return matchingInstitutionTargetAuthors;
			}
			public void setMatchingInstitutionTargetAuthors(List<String> matchingInstitutionTargetAuthor) {
				this.matchingInstitutionTargetAuthors = matchingInstitutionTargetAuthor;
			}
			public String getMatchingInstitutionFrequentCollaborator() {
				return matchingInstitutionFrequentCollaborator;
			}
			public void setMatchingInstitutionFrequentCollaborator(String matchingInstitutionFrequentCollaborator) {
				this.matchingInstitutionFrequentCollaborator = matchingInstitutionFrequentCollaborator;
			}
			public List<ReCiterArticleGrant> getMatchingGrantIDs() {
				return matchingGrantIDs;
			}
			public void setMatchingGrantIDs(List<ReCiterArticleGrant> matchingGrantID) {
				this.matchingGrantIDs = matchingGrantID;
			}
			public List<String> getMatchingEmails() {
				return matchingEmails;
			}
			public void setMatchingEmails(List<String> matchingEmail) {
				this.matchingEmails = matchingEmail;
			}
			public String getPublishedPriorAcademicDegree() {
				return publishedPriorAcademicDegree;
			}
			public void setPublishedPriorAcademicDegree(String publishedPriorAcademicDegree) {
				this.publishedPriorAcademicDegree = publishedPriorAcademicDegree;
			}
			public String getClusteredWithOtherMatchingArticles() {
				return clusteredWithOtherMatchingArticles;
			}
			public void setClusteredWithOtherMatchingArticles(String clusteredWithOtherMatchingArticles) {
				this.clusteredWithOtherMatchingArticles = clusteredWithOtherMatchingArticles;
			}
		}
	}
}
