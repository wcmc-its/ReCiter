package xmlparser.scopus.model;

/**
 * A class model for a Scopus article affiliation XML tag.
 * @author jil3004
 *
 */
public class Affiliation {

	private final int afid; // <afid> XML tag.
	private final String affilname; // <affilname> XML tag.
	private final String nameVariant; // <name-variant> XML tag.
	private final String affiliationCity; // <affiliation-city> XML tag.
	private final String affiliationCountry; // <affiliation-country> XML tag.
	
	public Affiliation(int afid, String affilname, String nameVariant,
			String affiliationCity, String affiliationCountry) {
		this.afid = afid;
		this.affilname = affilname;
		this.nameVariant = nameVariant;
		this.affiliationCity = affiliationCity;
		this.affiliationCountry = affiliationCountry;
	}

	public int getAfid() {
		return afid;
	}

	public String getAffilname() {
		return affilname;
	}

	public String getNameVariant() {
		return nameVariant;
	}

	public String getAffiliationCity() {
		return affiliationCity;
	}

	public String getAffiliationCountry() {
		return affiliationCountry;
	}
}
