package reciter.model.pubmed;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pubmedarticle")
public class PubMedArticle {
	
	private MedlineCitation medlineCitation;
	private PubMedData pubMedData;
	
	public PubMedArticle() {}
	
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
