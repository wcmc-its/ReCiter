package main.xml.scopus.model;

import java.util.List;

public class ScopusEntry {
	
	private String pubmedID; // pubmedID of this ScopusEntry
	private List<ScopusAffiliation> affiliation; // List of ScopusAffiliations associated with this ScopusEntry.
	
	/**
	 * For a single ScopusEntry, concatenate all the affiliation into a single String.
	 * @return a String containing the concatenated affiliation information. 
	 */
	public String affiliationConcatForm() {
		StringBuilder sb = new StringBuilder();
		
		// for each affiliation of this pmid, append into a single String.
		for (ScopusAffiliation affil : affiliation) {
			
			// append affiliation name.
			sb.append(affil.getAffilName());
			sb.append(" ");
			
			// append name variants.
			for (String nameVariant : affil.getNameVariantList()) {
				sb.append(nameVariant);
				sb.append(" ");
			}
			
			// append affiliation city
			sb.append(affil.getAffiliationCity());
			sb.append(" ");
			
			// append affiliation country.
			sb.append(affil.getAffiliationCountry());
			sb.append(" "); // might include extra white space at the end. Doesn't matter when computing cosine sim.
		}
		return sb.toString();
	}
	
	public String getPubmedID() {
		return pubmedID;
	}
	public void setPubmedID(String pubmedID) {
		this.pubmedID = pubmedID;
	}
	public List<ScopusAffiliation> getAffiliation() {
		return affiliation;
	}
	public void setAffiliation(List<ScopusAffiliation> affiliation) {
		this.affiliation = affiliation;
	}	
}
