package reciter.service.bean;

public class IdentityEducationBean {

	private long id;
	private String cwid;
	private String institution;
	private int degreeYear;
	private String degreeField;
	private String instLoc; // institution location.
	private String instAbbr; // institution abbreviation.
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getInstitution() {
		return institution;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	public int getDegreeYear() {
		return degreeYear;
	}
	public void setDegreeYear(int degreeYear) {
		this.degreeYear = degreeYear;
	}
	public String getDegreeField() {
		return degreeField;
	}
	public void setDegreeField(String degreeField) {
		this.degreeField = degreeField;
	}
	public String getInstLoc() {
		return instLoc;
	}
	public void setInstLoc(String instLoc) {
		this.instLoc = instLoc;
	}
	public String getInstAbbr() {
		return instAbbr;
	}
	public void setInstAbbr(String instAbbr) {
		this.instAbbr = instAbbr;
	}
}
