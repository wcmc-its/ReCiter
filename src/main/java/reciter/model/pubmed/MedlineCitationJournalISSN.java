package reciter.model.pubmed;

public class MedlineCitationJournalISSN {

	private enum IssnType {
		ELECTRONIC,
		PRINT,
		UNDETERMINED
	}
	
	private IssnType issnType;
	private String issn;
	
	public MedlineCitationJournalISSN() {}
}
