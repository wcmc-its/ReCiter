package reciter.erroranalysis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AnalysisConstants {

	// Information Retrieval constants.
	public static final String STATUS = "Status";
	public static final String CWID = "cwid";
	public static final String TARGET_NAME = "Target Name";
	public static final String PUBMED_SEARCH_QUERY = "PubMed Search Query";
	
	// Pre-processing constants.
	public static final String PMID = "PMID";
	public static final String ARTICLE_TITLE = "Article Title";
	public static final String FULL_JOURNAL_TITLE = "Full Journal Title";
	public static final String PUBLICATION_YEAR = "Year of Publication";
	public static final String SCOPUS_TARGET_AUTHOR_AFFILIATION = "Scopus Target Author Affilition";
	public static final String SCOPUS_COAUTHOR_AFFILIATION = "Scopus Co-Author Affiliation";
	public static final String PUBMED_TARGET_AUTHOR_AFFILIATION = "PubMed target author affiliation";
	public static final String PUBMED_COAUTHOR_AFFILIATION = "PubMed Co-Author Affiliation";
	public static final String ARTICLE_KEYWORDS = "Article keywords";
	
	// Phase one clustering: clustering results and scores constants.
	public static final String NAME_MATCHING_SCORE = "Name matching score";
	public static final String IS_CLUSTER_ORIGINATOR = "Cluster Originator";
	public static final String JOURNAL_SIMILARITY_PHASE_ONE = "Journal Similarity Phase One";
	
	// Phase two matching: matching scores.
	public static final String COAUTHOR_AFFILIATION_SCORE = "Co-Author Affiliation Score";
	public static final String TARGET_AUTHOR_AFFILIATION_SCORE = "Target Author Affiliation Score";
	public static final String KNOWN_COINVESTIGATOR_SCORE = "Known Co-Investigator Score";
	public static final String FUNDING_STATEMENT_SCORE = "Funding Statement Score";
	public static final String TERMINAL_DEGREE_SCORE = "Terminal Degree Score";
	public static final String DEFAULT_DEPARTMENT_JOURNAL_SIMILARITY_SCORE = "Default Department Journal Similarity Score";
	public static final String DEPARTMENT_OF_AFFILIATION_SCORE = "Department of Affiliation Score";
	public static final String KEYWORD_MATCHING_SCORE = "Keyword Matching Score";
	
	// Phase two matching: scoring results.
	public static final String PHASE_TWO_SIMILARITY_THRESHOLD = "Phase Two Similarity Threshold";
	public static final String CLUSTER_ARTICLE_ASSIGNED_TO = "Cluster Article Assigned To";
	public static final String COUNT_ARTICLES_IN_ASSIGNED_CLUSTERS = "Count Articles In Assigned Cluster";
	public static final String CLUSTER_SELECTED_IN_PHASE_TWO_MATCHING = "Cluster Selected in Phase Two Matching";
	public static final String AFFILIATION_SIMILARITY = "Affiliation Similarity";
	public static final String KEYWORD_SIMILARITY = "Keyword Similarity";
	public static final String JOURNAL_SIMILARITY_PHASE_TWO = "Journal Similarity Phase Two";
	
	public static String[] getConstantsAsArray() {
		Field[] fields = AnalysisConstants.class.getDeclaredFields();
		String[] constantsArray = new String[fields.length];
		int i = 0;
		for (Field field : fields) {
			try {
				constantsArray[i] = (field.get(field.getName()).toString());
				i++;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return constantsArray;
	}
}
