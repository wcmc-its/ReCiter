package reciter.xml.parser.pubmed.model;

public class MedlineCitationGrant {

	private String grantID;
	private String acronym;
	private String agency;
	private String country;
	
	public String getGrantID() {
		return grantID;
	}
	public void setGrantID(String grantID) {
		this.grantID = grantID;
	}
	public String getAcronym() {
		return acronym;
	}
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
	public String getAgency() {
		return agency;
	}
	public void setAgency(String agency) {
		this.agency = agency;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
}
