package main.xml.scopus.model;

import java.util.List;

/**
 * Models Scopus affiliation XML tag.
 * 
 * @author jielin
 *
 */
public class ScopusAffiliation {
	
	private String affilName;
	private List<String> nameVariantList;
	private String affiliationCity;
	private String affiliationCountry;
	
	public String getAffilName() {
		return affilName;
	}
	public void setAffilName(String affilName) {
		this.affilName = affilName;
	}
	public List<String> getNameVariantList() {
		return nameVariantList;
	}
	public void setNameVariantList(List<String> nameVariantList) {
		this.nameVariantList = nameVariantList;
	}
	public String getAffiliationCity() {
		return affiliationCity;
	}
	public void setAffiliationCity(String affiliationCity) {
		this.affiliationCity = affiliationCity;
	}
	public String getAffiliationCountry() {
		return affiliationCountry;
	}
	public void setAffiliationCountry(String affiliationCountry) {
		this.affiliationCountry = affiliationCountry;
	}
}
