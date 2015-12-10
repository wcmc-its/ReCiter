package database.model;

/**
 * 
 * @author htadimeti
 *
 */
public class IdentityDirectory {
	private int id;
	private String recordType;
	private String cwid;
	private String givenName;
	private String middleName;
	private String surname;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRecordType() {
		return recordType;
	}
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}	
}
