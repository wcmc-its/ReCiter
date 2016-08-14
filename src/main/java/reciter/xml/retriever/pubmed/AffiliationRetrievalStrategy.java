package reciter.xml.retriever.pubmed;

import reciter.model.author.TargetAuthor;

public class AffiliationRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	private static final String retrievalStrategyName = "AffiliationRetrievalStrategy";
	private static final String AFFILIATION_QUERY = "AND ((new york) OR 10065 OR 10021 OR weill OR cornell OR (newyork AND presbyterian) OR (new york AND presbyterian) OR HSS OR (hospital special surgery) OR (North Shore hospital) OR (Long Island Jewish) OR (memorial sloan) OR (sloan kettering) OR sloan-kettering OR hamad OR (mount sinai) OR (methodist houston) OR (National Institute of Mental Health) OR (beth israel) OR (University of Pennsylvania Medicine) OR (Merck Research) OR (New York Medical College) OR (Medicine Dentistry New Jersey) OR Montefiore OR (Lenox Hill) OR (Cold Spring Harbor) OR (St. Luke's-Roosevelt) OR (New York University Medicine) OR Langone OR (SUNY Downstate) OR (Albert Einstein Medicine) OR Yeshiva OR UMDNJ OR Icahn Medicine OR (Mount Sinai) OR (columbia medical) OR (columbia physicians))";

	public AffiliationRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}
	
	@Override
	public int getThreshold() {
		return threshold;
	}

	@Override
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	protected String constructInitialQuery(TargetAuthor targetAuthor) {
		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		return lastName + " " + firstInitial + " " + AFFILIATION_QUERY;
	}

	@Override
	protected String constructStrictQuery(TargetAuthor targetAuthor) {
		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstName = targetAuthor.getAuthorName().getFirstName();
		return lastName + " " + firstName + " " + AFFILIATION_QUERY;
	}

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
}
