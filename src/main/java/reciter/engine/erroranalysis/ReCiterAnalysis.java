/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.engine.erroranalysis;

import java.util.List;

import reciter.model.article.ReCiterArticleGrant;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.KnownRelationship;

public class ReCiterAnalysis {
	private String cwid;
	private List<Long> notRetrievedGoldStandards;
	private int numSuggestedArticles;
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

	public List<Long> getNotRetrievedGoldStandards() {
		return notRetrievedGoldStandards;
	}
	public void setNotRetrievedGoldStandards(List<Long> notRetrievedGoldStandards) {
		this.notRetrievedGoldStandards = notRetrievedGoldStandards;
	}

	public int getNumSuggestedArticles() {
		return numSuggestedArticles;
	}
	public void setNumSuggestedArticles(int numSuggestedArticles) {
		this.numSuggestedArticles = numSuggestedArticles;
	}

	public static class ReCiterAnalysisArticle {
		private long pmid;
		private Citation citation;
		private int score;
		
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

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
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
			private String publishedPriorAcademicDegreeBachelors;
			private String publishedPriorAcademicDegreeDoctoral;
			private ClusteredWithOtherMatchingArticle clusteredWithOtherMatchingArticle;
			
			public static class ClusteredWithOtherMatchingArticle {
				private String meshMajor;
				private String cites;
				private String citedBy;
				private String coCitation;
				private String journalTitle;
				
				public String getMeshMajor() {
					return meshMajor;
				}
				public void setMeshMajor(String meshMajor) {
					this.meshMajor = meshMajor;
				}
				public String getCites() {
					return cites;
				}
				public void setCites(String cites) {
					this.cites = cites;
				}
				public String getCitedBy() {
					return citedBy;
				}
				public void setCitedBy(String citedBy) {
					this.citedBy = citedBy;
				}
				public String getCoCitation() {
					return coCitation;
				}
				public void setCoCitation(String coCitation) {
					this.coCitation = coCitation;
				}
				public String getJournalTitle() {
					return journalTitle;
				}
				public void setJournalTitle(String journalTitle) {
					this.journalTitle = journalTitle;
				}
			}
			
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
			public String getPublishedPriorAcademicDegreeBachelors() {
				return publishedPriorAcademicDegreeBachelors;
			}
			public void setPublishedPriorAcademicDegreeBachelors(String publishedPriorAcademicDegreeBachelors) {
				this.publishedPriorAcademicDegreeBachelors = publishedPriorAcademicDegreeBachelors;
			}
			public String getPublishedPriorAcademicDegreeDoctoral() {
				return publishedPriorAcademicDegreeDoctoral;
			}
			public void setPublishedPriorAcademicDegreeDoctoral(String publishedPriorAcademicDegreeDoctoral) {
				this.publishedPriorAcademicDegreeDoctoral = publishedPriorAcademicDegreeDoctoral;
			}
			public ClusteredWithOtherMatchingArticle getClusteredWithOtherMatchingArticle() {
				return clusteredWithOtherMatchingArticle;
			}
			public void setClusteredWithOtherMatchingArticle(ClusteredWithOtherMatchingArticle clusteredWithOtherMatchingArticle) {
				this.clusteredWithOtherMatchingArticle = clusteredWithOtherMatchingArticle;
			}
		}
	}
}
