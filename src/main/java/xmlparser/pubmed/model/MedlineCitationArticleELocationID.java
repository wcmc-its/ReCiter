package xmlparser.pubmed.model;

public class MedlineCitationArticleELocationID {

	private enum EIdType {
		DOI,
		PII
	}
	
	private EIdType eIdType;
	private MedlineCitationYNEnum validYN;
	private String eLocationId;
}
