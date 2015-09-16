package reciter.model.author;

/**
 * @author jil3004
 *
 */
public class TargetAuthor extends ReCiterAuthor {

	private String cwid;
	private String department;
	private String otherDeparment;
	private AuthorEducation education;
	private AuthorDegree degree;
	
	public TargetAuthor(AuthorName name, AuthorAffiliation affiliation) {
		super(name, affiliation);
	}

	public String getCwid() {
		return cwid;
	}

	public void setCwid(String cwid) {
		this.cwid = cwid;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public AuthorEducation getEducation() {
		return education;
	}

	public void setEducation(AuthorEducation education) {
		this.education = education;
	}

	public String getOtherDeparment() {
		return otherDeparment;
	}

	public void setOtherDeparment(String otherDeparment) {
		this.otherDeparment = otherDeparment;
	}

	public AuthorDegree getDegree() {
		return degree;
	}

	public void setDegree(AuthorDegree degree) {
		this.degree = degree;
	}

}
