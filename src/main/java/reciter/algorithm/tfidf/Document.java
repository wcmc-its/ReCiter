package reciter.algorithm.tfidf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Document {

	private long id;
	private Map<String, Long> termToFreqMap;
	private Map<String, Term> terms;

	public Document(String s) {
		termToFreqMap = new HashMap<String, Long>();
		terms = new HashMap<String, Term>();
		String[] tokens = tokenize(s);
		updateTermFrequency(tokens);
	}
	
	protected String[] tokenize(String s) {
		return s.replaceAll("[^A-Za-z0-9\\s+]", "").split("\\s+");
	}
	
	private void updateTermFrequency(String[] tokens) {
		for (String token : tokens) {
			if (termToFreqMap.containsKey(token)) {
				long currentFreq = termToFreqMap.get(token);
				currentFreq += 1;
				termToFreqMap.put(token, currentFreq);
			} else {
				termToFreqMap.put(token, 1L);
				terms.put(token, new Term(token)); // add to terms.
			}
		}
	}
	
	public Map<String, Long> getTermToFreqMap() {
		return termToFreqMap;
	}

	public boolean contains(String s) {
		return termToFreqMap.containsKey(s);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Map<String, Term> getTerms() {
		return terms;
	}

}
