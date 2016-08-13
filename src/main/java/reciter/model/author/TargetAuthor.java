package reciter.model.author;

import java.util.List;
import java.util.Set;

/**
 * @author jil3004
 *
 */
public class TargetAuthor extends ReCiterAuthor {

	private String cwid;
	private String department;
	private String otherDepartment;
	private List<AuthorEducation> educations;
	private AuthorDegree degree;
	private String citizenship;
	private List<AuthorName> aliasList;
	private List<String> boardCertifications;
	private List<AuthorName> grantCoauthors;
	private String email;
	private String emailOther;
	private String pubmedSearchQuery;
	private List<String> alternateDepartmentNames;
	private List<AuthorName> authorNamesFromEmailFetch;
	private int articleSize;
	private List<String> institutions;
	private List<String> sponsorAwardIds;
	private List<String> emailAddresses; // rc_identity_email
	private Set<AuthorName> authorNameVariationsRetrievedFromPubmedUsingEmail;
	
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
	
	public String getOtherDepartment() {
		return otherDepartment;
	}

	public void setOtherDepartment(String otherDepartment) {
		this.otherDepartment = otherDepartment;
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

	public List<AuthorName> getAliasList() {
		return aliasList;
	}

	public void setAliasList(List<AuthorName> aliasList) {
		this.aliasList = aliasList;
	}

	public List<String> getAlternateDepartmentNames() {
		return alternateDepartmentNames;
	}

	public void setAlternateDepartmentNames(List<String> alternateDepartmentNames) {
		this.alternateDepartmentNames = alternateDepartmentNames;
	}

	public List<AuthorName> getAuthorNamesFromEmailFetch() {
		return authorNamesFromEmailFetch;
	}

	public void setAuthorNamesFromEmailFetch(List<AuthorName> authorNamesFromEmailFetch) {
		this.authorNamesFromEmailFetch = authorNamesFromEmailFetch;
	}

	public int getArticleSize() {
		return articleSize;
	}

	public void setArticleSize(int articleSize) {
		this.articleSize = articleSize;
	}

	public List<String> getInstitutions() {
		return institutions;
	}

	public void setInstitutions(List<String> institutions) {
		this.institutions = institutions;
	}

	public List<String> getSponsorAwardIds() {
		return sponsorAwardIds;
	}

	public void setSponsorAwardIds(List<String> sponsorAwardIds) {
		this.sponsorAwardIds = sponsorAwardIds;
	}

	public List<String> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(List<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	public Set<AuthorName> getAuthorNameVariationsRetrievedFromPubmedUsingEmail() {
		return authorNameVariationsRetrievedFromPubmedUsingEmail;
	}

	public void setAuthorNameVariationsRetrievedFromPubmedUsingEmail(
			Set<AuthorName> authorNameVariationsRetrievedFromPubmedUsingEmail) {
		this.authorNameVariationsRetrievedFromPubmedUsingEmail = authorNameVariationsRetrievedFromPubmedUsingEmail;
	}
}
