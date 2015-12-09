package reciter.model.author;

import java.util.List;

import database.model.IdentityDirectory;

/**
 * @author jil3004
 *
 */
public class TargetAuthor extends ReCiterAuthor {

	private String cwid;
	private String department;
	private String otherDeparment;
	private List<AuthorEducation> educations;
	private AuthorDegree degree;
	private String citizenship;
	private List<IdentityDirectory> aliasList;
	private List<String> boardCertifications;
	private List<AuthorName> grantCoauthors;
	private String email;
	private String emailOther;
	private String pubmedSearchQuery;
	
	/**
	 * @return the aliasList
	 */
	public List<IdentityDirectory> getAliasList() {
		return aliasList;
	}

	/**
	 * @param aliasList the aliasList to set
	 */
	public void setAliasList(List<IdentityDirectory> aliasList) {
		this.aliasList = aliasList;
	}

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

	public String getCitizenship() {
		return citizenship;
	}

	public void setCitizenship(String citizenship) {
		this.citizenship = citizenship;
	}

	public List<String> getBoardCertifications() {
		return boardCertifications;
	}

	public void setBoardCertifications(List<String> boardCertifications) {
		this.boardCertifications = boardCertifications;
	}

	public List<AuthorName> getGrantCoauthors() {
		return grantCoauthors;
	}

	public void setGrantCoauthors(List<AuthorName> grantCoauthors) {
		this.grantCoauthors = grantCoauthors;
	}

	public String getEmailOther() {
		return emailOther;
	}

	public void setEmailOther(String emailOther) {
		this.emailOther = emailOther;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<AuthorEducation> getEducations() {
		return educations;
	}

	public void setEducations(List<AuthorEducation> educations) {
		this.educations = educations;
	}

	public String getPubmedSearchQuery() {
		return pubmedSearchQuery;
	}

	public void setPubmedSearchQuery(String pubmedSearchQuery) {
		this.pubmedSearchQuery = pubmedSearchQuery;
	}
}
