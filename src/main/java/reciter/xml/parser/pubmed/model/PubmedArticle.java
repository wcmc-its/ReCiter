package reciter.xml.parser.pubmed.model;

public class PubmedArticle {
	
	private MedlineCitation medlineCitation;
	private PubmedData pubmedData;
	
	public MedlineCitation getMedlineCitation() {
		return medlineCitation;
	}
	public void setMedlineCitation(MedlineCitation medlineCitation) {
		this.medlineCitation = medlineCitation;
	}
	public PubmedData getPubmedData() {
		return pubmedData;
	}
	public void setPubmedData(PubmedData pubmedData) {
		this.pubmedData = pubmedData;
	}
}
