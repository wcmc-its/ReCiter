package reciter.xml.parser.pubmed.model;

public class PubMedArticle {
	
	private MedlineCitation medlineCitation;
	private PubMedData pubMedData;
	
	public MedlineCitation getMedlineCitation() {
		return medlineCitation;
	}
	public void setMedlineCitation(MedlineCitation medlineCitation) {
		this.medlineCitation = medlineCitation;
	}
	public PubMedData getPubMedData() {
		return pubMedData;
	}
	public void setPubmedData(PubMedData pubMedData) {
		this.pubMedData = pubMedData;
	}
}
