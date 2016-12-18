package reciter.model.identity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Identity {

	private String cwid; // cwid of the user
	private AuthorName primaryName; // primary name of the user
	private List<AuthorName> alternateNames; // aliases
	private List<String> emails; // list of emails
	private List<AuthorName> knownRelationships; // known relationships
	private List<String> departments; // list of department
	private String title; // title of the person
	private List<String> institutions; // institutions
	private Education degreeYear; // degreeYear
	private String personType; // type of person: i.e., academic, academic-faculty, etc...
	private String program; // program the person is in.
	private List<Long> knownPmids; // known pmids.
	private List<String> boardCertifications; // board certifications
	private String citizenship; // citizenship
	private List<Grant> grants; // grants
	private List<String> keywords; // keywords
	private List<PubMedAlias> pubMedAlias; // name alias from PubMed
	private LocalDateTime dateInitialRun; // the date of the first time that ReCiter perform the retrieval
	private LocalDateTime dateLastRun; // the date of the most recent retrieval
	
	public Identity() {}

	public String getCwid() {
		return cwid;
	}

	public void setCwid(String cwid) {
		this.cwid = cwid;
	}

	public AuthorName getPrimaryName() {
		return primaryName;
	}

	public void setPrimaryName(AuthorName primaryName) {
		this.primaryName = primaryName;
	}

	public List<AuthorName> getAlternateNames() {
		return alternateNames;
	}

	public void setAlternateNames(List<AuthorName> alternateNames) {
		this.alternateNames = alternateNames;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public List<AuthorName> getKnownRelationships() {
		return knownRelationships;
	}

	public void setKnownRelationships(List<AuthorName> knownRelationships) {
		this.knownRelationships = knownRelationships;
	}

	public List<String> getDepartments() {
		return departments;
	}

	public void setDepartments(List<String> departments) {
		this.departments = departments;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getInstitutions() {
		return institutions;
	}

	public void setInstitutions(List<String> institutions) {
		this.institutions = institutions;
	}

	public Education getDegreeYear() {
		return degreeYear;
	}

	public void setDegreeYear(Education degreeYear) {
		this.degreeYear = degreeYear;
	}

	public String getPersonType() {
		return personType;
	}

	public void setPersonType(String personType) {
		this.personType = personType;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public List<Long> getKnownPmids() {
		return knownPmids;
	}

	public void setKnownPmids(List<Long> knownPmids) {
		this.knownPmids = knownPmids;
	}

	public List<String> getBoardCertifications() {
		return boardCertifications;
	}

	public void setBoardCertifications(List<String> boardCertifications) {
		this.boardCertifications = boardCertifications;
	}

	public String getCitizenship() {
		return citizenship;
	}

	public void setCitizenship(String citizenship) {
		this.citizenship = citizenship;
	}

	public List<Grant> getGrants() {
		return grants;
	}

	public void setGrants(List<Grant> grants) {
		this.grants = grants;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<PubMedAlias> getPubMedAlias() {
		if (pubMedAlias == null) {
			return Collections.emptyList();
		}
		return pubMedAlias;
	}

	public void setPubMedAlias(List<PubMedAlias> pubMedAlias) {
		this.pubMedAlias = pubMedAlias;
	}

	public LocalDateTime getDateInitialRun() {
		return dateInitialRun;
	}

	public void setDateInitialRun(LocalDateTime dateInitialRun) {
		this.dateInitialRun = dateInitialRun;
	}

	public LocalDateTime getDateLastRun() {
		return dateLastRun;
	}

	public void setDateLastRun(LocalDateTime dateLastRun) {
		this.dateLastRun = dateLastRun;
	}
	
}
