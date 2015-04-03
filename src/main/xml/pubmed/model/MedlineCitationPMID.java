package main.xml.pubmed.model;

public class MedlineCitationPMID {

	private final String pmid;
	private String version;
		
	public MedlineCitationPMID(String pmid) {
		this.pmid = pmid;
	}
	
	public String getPmidString() {
		return pmid;
	}
	public String getVersion() {
		return version;
	}
}
