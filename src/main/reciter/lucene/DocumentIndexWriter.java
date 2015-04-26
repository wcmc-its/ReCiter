package main.reciter.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class DocumentIndexWriter {

	private File documentIndexDirectory;
	private Directory luceneDirectory;
	private Analyzer luceneAnalyzer;
	private IndexWriterConfig luceneIndexWriterConfig;
	private IndexWriter luceneIndexWriter;
	private static boolean useStopWords;

	protected static final String DIR_PATH = "data/lucene_index/";

	public DocumentIndexWriter() {
		setUseStopWords(false);
	}

	public static final List<String> stopWords = Arrays.asList(
			"a", "an", "and", "are", "as", "at", "be", "but", "by",
			"for", "if", "in", "into", "is", "it",
			"no", "not", "of", "on", "or", "such",
			"that", "the", "their", "then", "there", "these",
			"they", "this", "to", "was", "will", "with", 
			"university", "usa", "department", "center", "medicine", "medical",
			"school", "edu", "health", "sciences", "institute", "hospital", 
			"association", "research", "new", "college", "division", "research", 
			"national", "state", "county", "city", "hospital", "laboratory", "center", 
			"united", "states", "america", "new", "york", "usa", 

			"Alabama","AL",
			"Alaska","AK",
			"Arizona","AZ",
			"Arkansas","AR",
			"California","CA",
			"Colorado","CO",
			"Connecticut","CT",
			"Delaware","DE",
			"District of Columbia","DC",
			"Florida","FL",
			"Georgia","GA",
			"Hawaii","HI",
			"Idaho","ID",
			"Illinois","IL",
			"Indiana","IN",
			"Iowa","IA",
			"Kansas","KS",
			"Kentucky","KY",
			"Louisiana","LA",
			"Maine","ME",
			"Montana","MT",
			"Nebraska","NE",
			"Nevada","NV",
			"New Hampshire","NH",
			"New Jersey","NJ",
			"New Mexico","NM",
			"New York","NY",
			"North Carolina","NC",
			"North Dakota","ND",
			"Ohio","OH",
			"Oklahoma","OK",
			"Oregon","OR",
			"Maryland","MD",
			"Massachusetts","MA",
			"Michigan","MI",
			"Minnesota","MN",
			"Mississippi","MS",
			"Missouri","MO",
			"Pennsylvania","PA",
			"Rhode Island","RI",
			"South Carolina","SC",
			"South Dakota","SD",
			"Tennessee","TN",
			"Texas","TX",
			"Utah","UT",
			"Vermont","VT",
			"Virginia","VA",
			"Washington","WA",
			"West Virginia","WV",
			"Wisconsin","WI",
			"Wyoming","WY"
			);

	private CharArraySet stopSet = new CharArraySet(stopWords, false);

	public DocumentIndexWriter(String cwid) {

		documentIndexDirectory = new File(DIR_PATH + cwid);

		try {
			luceneDirectory = FSDirectory.open(documentIndexDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (useStopWords) {
			for (String stopword : stopWords) {
				stopSet.add(stopword);
			}
		} else {
			stopSet = StandardAnalyzer.STOP_WORDS_SET;
		}

		luceneAnalyzer = new StandardAnalyzer(stopSet);
		luceneIndexWriterConfig = new IndexWriterConfig(Version.LATEST, luceneAnalyzer);

		if (documentIndexDirectory.exists()) {
			luceneIndexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		} else {
			luceneIndexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		}

		try {
			luceneIndexWriter = new IndexWriter(luceneDirectory, luceneIndexWriterConfig);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void index(Document document) {
		try {
			luceneIndexWriter.addDocument(document);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			indexClose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void indexAll(List<Document> documentList) {
		try {
			for (Document document : documentList) {
				luceneIndexWriter.addDocument(document);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			indexClose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void indexClose() throws IOException {
		luceneIndexWriter.close();
	}

	public static boolean isUseStopWords() {
		return useStopWords;
	}

	public static void setUseStopWords(boolean useStopWords) {
		DocumentIndexWriter.useStopWords = useStopWords;
	}

	public CharArraySet getStopSet() {
		return stopSet;
	}


}