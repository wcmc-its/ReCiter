package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import reciter.model.author.AuthorName;

@Document(collection = "identity")
public class Identity {

	private String cwid;
	private AuthorName authorName;
	private List<String> emails;
	private List<String> departments;
	private int yearOfTerminalDegree;
	private List<String> institutions;
	private List<AuthorName> knownRelationships;
	private List<Long> knownPmids;
	
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
	public int getYearOfTerminalDegree() {
		return yearOfTerminalDegree;
	}
	public void setYearOfTerminalDegree(int yearOfTerminalDegree) {
		this.yearOfTerminalDegree = yearOfTerminalDegree;
	}
	public List<String> getInstitutions() {
		return institutions;
	}
	public void setInstitutions(List<String> institutions) {
		this.institutions = institutions;
	}
	public List<AuthorName> getKnownRelationships() {
		return knownRelationships;
	}
	public void setKnownRelationships(List<AuthorName> knownRelationships) {
		this.knownRelationships = knownRelationships;
	}
	public List<Long> getKnownPmids() {
		return knownPmids;
	}
	public void setKnownPmids(List<Long> knownPmids) {
		this.knownPmids = knownPmids;
	}
}
