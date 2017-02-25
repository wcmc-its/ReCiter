package reciter.engine.erroranalysis;

import java.util.List;

import reciter.model.article.ReCiterAuthor;

public class ReCiterAnalysis {

	public class ReCiterAnalysisArticle {
		private long pmid;
		private Citation citation;
		
		public class Citation {
			private String pubDate;
			private List<ReCiterAuthor> authorList;
			private Journal journal;
			
			public class Journal {
				private String verbose;
				private String medlineTA;
			}
			
			private String volume;
			private String issue;
			private String pages;
			private String pmcid;
			private String doi;
		}
		
		private String userAssertion;
		private PositiveEvidence positiveEvidence;
		
		public class PositiveEvidence {
			private String matchingNameVariant;
			private String matchingDepartment;
			private String matchingRelationship;
			private String matchingInstitutionTargetAuthor;
			private String matchingInstitutionFrequentCollaborator;
			private String matchingGrantID;
			private String matchingEmail;
			private String publishedPriorAcademicDegree;
			private String clusteredWithOtherMatchingArticles;
		}
	}
}
