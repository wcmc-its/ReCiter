package reciter.model.author;

public class AuthorEducation {

	private String institution;
	private int degreeYear;
	private String degreeField;
	private String instLoc;
	private String instAbbr;
	private AuthorDegree authorDegree;
	
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
	public AuthorDegree getAuthorDegree() {
		return authorDegree;
	}
	public void setAuthorDegree(AuthorDegree authorDegree) {
		this.authorDegree = authorDegree;
	}
	
	
}
