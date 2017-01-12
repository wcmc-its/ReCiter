package reciter.model.pubmed;

public class MedlineCitationArticleELocationID {

	private enum EIdType {
		DOI,
		PII
	}
	
	private EIdType eIdType;
	private MedlineCitationYNEnum validYN;
	private String eLocationId;
	
	public MedlineCitationArticleELocationID() {}

	public EIdType geteIdType() {
		return eIdType;
	}

	public void seteIdType(EIdType eIdType) {
		this.eIdType = eIdType;
	}

	public MedlineCitationYNEnum getValidYN() {
		return validYN;
	}

	public void setValidYN(MedlineCitationYNEnum validYN) {
		this.validYN = validYN;
	}

	public String geteLocationId() {
		return eLocationId;
	}

	public void seteLocationId(String eLocationId) {
		this.eLocationId = eLocationId;
	}
}
