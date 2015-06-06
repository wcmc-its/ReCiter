package xmlparser.pubmed.model;

public class MedlineCitationJournalISSN {

	private enum IssnType {
		ELECTRONIC,
		PRINT,
		UNDETERMINED
	}
	
	private IssnType issnType;
	private String issn;
}
