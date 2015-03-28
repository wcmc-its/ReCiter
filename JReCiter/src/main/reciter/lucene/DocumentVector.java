package main.reciter.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.reciter.lucene.docvectorsimilarity.CosineSimilarity;
import main.reciter.lucene.docvectorsimilarity.DocumentVectorSimilarity;
import main.reciter.model.author.ReCiterAuthor;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.SparseRealVector;

public class DocumentVector {

	private int pmid; // pmid of the ReCiterArticle.
	private Map<String, Integer> termToFreqMap; // term to frequency map.
	private DocumentVectorType documentVectorType; // type of vector.
	private SparseRealVector sparseRealVector; // sparse vector containing frequency of the term.
	private DocumentVectorSimilarity documentVectorSimilarity; // type of similarity measure.
	private List<ReCiterAuthor> authorList = new ArrayList<ReCiterAuthor>();
	
	public DocumentVector(DocumentVectorType documentVectorType, DocumentTerm documentTerms) {
		termToFreqMap = new HashMap<String, Integer>();
		if (documentVectorType == DocumentVectorType.ARTICLE_TITLE) {
			sparseRealVector = new OpenMapRealVector(documentTerms.getAllTitleTerms().size());
			
		} else if (documentVectorType == DocumentVectorType.JOURNAL_TITLE) {
			sparseRealVector = new OpenMapRealVector(documentTerms.getAllJournalTitleTerms().size());
			
		} else if (documentVectorType == DocumentVectorType.AFFILIATION) {
			sparseRealVector = new OpenMapRealVector(documentTerms.getAllAffiliationTerms().size());
			
		} else if (documentVectorType == DocumentVectorType.KEYWORD) {
			sparseRealVector = new OpenMapRealVector(documentTerms.getAllKeywordTerms().size());
		}
		
		this.documentVectorSimilarity = new CosineSimilarity();
	}
	
	public void setEntry(int pos, double freq) {
		sparseRealVector.setEntry(pos, freq); // set the raw term count at position.
	}

	public SparseRealVector getVector() {
		return sparseRealVector;
	}
	public void setVector(SparseRealVector vector) {
		this.sparseRealVector = vector;
	}
	public DocumentVectorType getDocumentVectorType() {
		return documentVectorType;
	}
	public void setDocumentVectorType(DocumentVectorType documentVectorType) {
		this.documentVectorType = documentVectorType;
	}
	public DocumentVectorSimilarity getDocumentVectorSimilarity() {
		return documentVectorSimilarity;
	}
	public void setDocumentVectorSimilarity(DocumentVectorSimilarity documentVectorSimilarity) {
		this.documentVectorSimilarity = documentVectorSimilarity;
	}

	public Map<String, Integer> getTermToFreqMap() {
		return termToFreqMap;
	}

	public void setTermToFreqMap(Map<String, Integer> termToFreqMap) {
		this.termToFreqMap = termToFreqMap;
	}

	public int getPmid() {
		return pmid;
	}

	public void setPmid(int pmid) {
		this.pmid = pmid;
	}

	public List<ReCiterAuthor> getAuthorList() {
		return authorList;
	}

	public void setAuthorList(List<ReCiterAuthor> authorList) {
		this.authorList = authorList;
	}
}
