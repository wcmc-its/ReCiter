package reciter.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * All document terms for a feature.
 * @author jil3004
 *
 */
public class DocumentTerm {

	private Map<String, Integer> allAffiliationTerms;
	private Map<String, Integer> allKeywordTerms;
	private Map<String, Integer> allTitleTerms;
	private Map<String, Integer> allJournalTitleTerms;
	
	// word to document map. idf map. ie: which document(s) has this word?
	private Map<String, List<Integer>> affiliationIDFMap;

	// author names frequency map.
	private Map<String, Integer> allAuthorTerms;
	
	public DocumentTerm() {
		allAffiliationTerms = new HashMap<String, Integer>();
		allKeywordTerms = new HashMap<String, Integer>();
		allTitleTerms = new HashMap<String, Integer>();
		allJournalTitleTerms = new HashMap<String, Integer>();
		allAuthorTerms = new HashMap<String, Integer>();
	}

	public void initAllTerms(IndexReader indexReader) {
		try {
			initAllTerms(indexReader, DocumentVectorType.AFFILIATION);
			initAllTerms(indexReader, DocumentVectorType.ARTICLE_TITLE);
			initAllTerms(indexReader, DocumentVectorType.JOURNAL_TITLE);
			initAllTerms(indexReader, DocumentVectorType.KEYWORD);
			initAuthorName(indexReader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initIDFMap(IndexReader indexReader, DocumentVectorType type) throws IOException {
		affiliationIDFMap = new HashMap<String, List<Integer>>();
		
		for (int docId = 0; docId < indexReader.maxDoc(); docId++) {
			Terms vector = indexReader.getTermVector(docId, type.name());

			TermsEnum termsEnum = null;
			if (vector != null) {
				termsEnum = vector.iterator(termsEnum);
				BytesRef text = null;
				while ((text = termsEnum.next()) != null) {
					String term = text.utf8ToString();
					// Update the IDF map for affiliation.
					if (!affiliationIDFMap.containsKey(term)) {
						List<Integer> documentList = new ArrayList<Integer>();
						documentList.add(docId);
						affiliationIDFMap.put(term, documentList);
					} else {
						affiliationIDFMap.get(term).add(docId);
					}
				}
			}
		}
	}

	/**
	 * Initialize the allAuthorTerms by reading from the index.
	 * @param indexReader
	 * @throws IOException
	 */
	public void initAuthorName(IndexReader indexReader) throws IOException {
		
		int pos = 0;
		for (int docId = 0; docId < indexReader.maxDoc(); docId++) {
			Terms vector = indexReader.getTermVector(docId, DocumentVectorType.AUTHOR_SIZE.name());
			TermsEnum termsEnum = null;
			int authorListSize = 0;
			if (vector != null) {
				termsEnum = vector.iterator(termsEnum);
				BytesRef text = null;
				while ((text = termsEnum.next()) != null) {
					String term = text.utf8ToString();
					authorListSize = Integer.parseInt(term);
				}
			}
			if (authorListSize != 0) {
				for (int i = 0; i < authorListSize; i++) {
					Terms authorVector = indexReader.getTermVector(docId, DocumentVectorType.AUTHOR.name() + "_" + i);
					TermsEnum authorTermsEnum = null;
					if (authorVector != null) {
						authorTermsEnum = authorVector.iterator(authorTermsEnum);
						BytesRef authorText = null;
						while ((authorText = authorTermsEnum.next()) != null) {
							allAuthorTerms.put(authorText.utf8ToString(), pos++);
						}
					}
				}
			}
		}
		pos = 0;
		for (Map.Entry<String, Integer> s : allAuthorTerms.entrySet()) {
			s.setValue(pos++); // set the index position of this term in the SparseVectorArray.
		}
	}
	
	public void initAllTerms(IndexReader indexReader, DocumentVectorType type) throws IOException {
		int pos = 0; // index of the term in the SparseVectorArray.
		for (int docId = 0; docId < indexReader.maxDoc(); docId++) {
			Terms vector = indexReader.getTermVector(docId, type.name());

			TermsEnum termsEnum = null;
			if (vector != null) {
				termsEnum = vector.iterator(termsEnum);
				BytesRef text = null;
				while ((text = termsEnum.next()) != null) {
					String term = text.utf8ToString();
					
					// Stop words usage here:
					if (DocumentIndexWriter.stopWords.contains(term.toLowerCase())) {
						continue;
					}
					
					if (type == DocumentVectorType.ARTICLE_TITLE) {
						allTitleTerms.put(term, pos++);
					} else if (type == DocumentVectorType.JOURNAL_TITLE) {
						allJournalTitleTerms.put(term, pos++);
					} else if (type == DocumentVectorType.AFFILIATION) {
						allAffiliationTerms.put(term, pos++);
					} else if (type == DocumentVectorType.KEYWORD) {
						allKeywordTerms.put(term, pos++);
					}
				}
			}
		}
		// Update index of this term in the SparseVectorArray.
		pos = 0;
		if (type == DocumentVectorType.ARTICLE_TITLE) {
			for (Map.Entry<String, Integer> s : allTitleTerms.entrySet()) {
				s.setValue(pos++);
			}
		} else if (type == DocumentVectorType.JOURNAL_TITLE) {
			for (Map.Entry<String, Integer> s : allJournalTitleTerms.entrySet()) {
				s.setValue(pos++);
			}
		} else if (type == DocumentVectorType.AFFILIATION) {
			for (Map.Entry<String, Integer> s : allAffiliationTerms.entrySet()) {
				s.setValue(pos++);
			}
			
			// Delete:
//			System.out.println("Size of affiliation: " + allAffiliationTerms.size());
//			for (Map.Entry<String, Integer> s : allAffiliationTerms.entrySet()) {
//				System.out.println(s.getKey() + " has " + s.getValue());
//			}
		} else if (type == DocumentVectorType.KEYWORD) {
			for (Map.Entry<String, Integer> s : allKeywordTerms.entrySet()) {
				s.setValue(pos++);
			}
		}
	}

	public Map<String, Integer> getAllAffiliationTerms() {
		return allAffiliationTerms;
	}

	public Map<String, Integer> getAllKeywordTerms() {
		return allKeywordTerms;
	}

	public Map<String, Integer> getAllTitleTerms() {
		return allTitleTerms;
	}

	public Map<String, Integer> getAllJournalTitleTerms() {
		return allJournalTitleTerms;
	}

	public Map<String, List<Integer>> getAffiliationIDFMap() {
		return affiliationIDFMap;
	}

	public Map<String, Integer> getAllAuthorTerms() {
		return allAuthorTerms;
	}
}
