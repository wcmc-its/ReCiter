package reciter.database.mongo.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "identity")
public class Identity {

	private String cwid;
	private String lastName;
	private String firstName;
	private String middleName;
	private String title;
	private String appointmentType;
	private String appointmentStartYear;
	private String appointmentEndYear;
	private List<String> departments;
	private List<String> affiliations;
	private List<String> emails;
	private LocalDateTime createDate;
	private LocalDateTime lastModifiedDate;
	
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAppointmentType() {
		return appointmentType;
	}
	public void setAppointmentType(String appointmentType) {
		this.appointmentType = appointmentType;
	}
	public String getAppointmentStartYear() {
		return appointmentStartYear;
	}
	public void setAppointmentStartYear(String appointmentStartYear) {
		this.appointmentStartYear = appointmentStartYear;
	}
	public String getAppointmentEndYear() {
		return appointmentEndYear;
	}
	public void setAppointmentEndYear(String appointmentEndYear) {
		this.appointmentEndYear = appointmentEndYear;
	}
	public List<String> getDepartments() {
		return departments;
	}
	public void setDepartments(List<String> departments) {
		this.departments = departments;
	}
	public List<String> getAffiliations() {
		return affiliations;
	}
	public void setAffiliations(List<String> affiliations) {
		this.affiliations = affiliations;
	}
	public List<String> getEmails() {
		return emails;
	}
	public void setEmails(List<String> emails) {
		this.emails = emails;
	}
	public LocalDateTime getCreateDate() {
		return createDate;
	}
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	public LocalDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
