package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import reciter.model.author.AuthorName;

@Document(collection = "identity")
public class Identity {

	private String cwid; // cwid of the user
	private AuthorName authorName; // name of the user
	private List<AuthorName> aliases; // aliases
	private List<String> emails; // list of emails
	private List<String> departments; // list of department
	private String title; // title of the person
	private List<String> affiliations; // affiliations
	private List<AuthorName> relatedAuthorNames; // related authors
	private List<Long> knownPmids; // known pmids.
	private List<String> boardCertifications; // board certifications
	private String citizenship; // citizenship
	private Education bachelor;
	private Education masters;
	private Education doctoral;
	private List<Grant> grants;
	private List<String> keywords;
	private List<PubMedAlias> pubMedAlias;
	
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public AuthorName getAuthorName() {
		return authorName;
	}
	public void setAuthorName(AuthorName authorName) {
		this.authorName = authorName;
	}
	public List<AuthorName> getAliases() {
		return aliases;
	}
	public void setAliases(List<AuthorName> aliases) {
		this.aliases = aliases;
	}
	public List<String> getEmails() {
		return emails;
	}
	public void setEmails(List<String> emails) {
		this.emails = emails;
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
	public List<String> getAffiliations() {
		return affiliations;
	}
	public void setAffiliations(List<String> affiliations) {
		this.affiliations = affiliations;
	}
	public List<AuthorName> getRelatedAuthorNames() {
		return relatedAuthorNames;
	}
	public void setRelatedAuthorNames(List<AuthorName> relatedAuthorNames) {
		this.relatedAuthorNames = relatedAuthorNames;
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
	public Education getBachelor() {
		return bachelor;
	}
	public void setBachelor(Education bachelor) {
		this.bachelor = bachelor;
	}
	public Education getMasters() {
		return masters;
	}
	public void setMasters(Education masters) {
		this.masters = masters;
	}
	public Education getDoctoral() {
		return doctoral;
	}
	public void setDoctoral(Education doctoral) {
		this.doctoral = doctoral;
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
		return pubMedAlias;
	}
	public void setPubMedAlias(List<PubMedAlias> pubMedAlias) {
		this.pubMedAlias = pubMedAlias;
	}
}
