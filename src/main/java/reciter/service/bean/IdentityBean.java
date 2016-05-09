package reciter.service.bean;

import java.time.LocalDateTime;

public class IdentityBean {

	private int identityPk;
	private String cwid;
	private String status;
	private int ofaPersonPk;
	private String lastName;
	private String firstName;
	private String firstInitial;
	private String middleName;
	private String middleInitial;
	private String fullPublishedName;
	private String prefix;
	private String suffix;
	private String title;
	private String appointmentTypes;
	private String appointmentPeriod;
	private String primaryDepartment;
	private String otherDepartment;
	private String primaryAffiliation;
	private int harvesterFlag;
	private LocalDateTime createDate;
	private LocalDateTime updateDate;
	private String email;
	private String emailOther;
	
	public int getIdentityPk() {
		return identityPk;
	}
	public void setIdentityPk(int identityPk) {
		this.identityPk = identityPk;
	}
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getOfaPersonPk() {
		return ofaPersonPk;
	}
	public void setOfaPersonPk(int ofaPersonPk) {
		this.ofaPersonPk = ofaPersonPk;
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
	public String getFirstInitial() {
		return firstInitial;
	}
	public void setFirstInitial(String firstInitial) {
		this.firstInitial = firstInitial;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getMiddleInitial() {
		return middleInitial;
	}
	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}
	public String getFullPublishedName() {
		return fullPublishedName;
	}
	public void setFullPublishedName(String fullPublishedName) {
		this.fullPublishedName = fullPublishedName;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAppointmentTypes() {
		return appointmentTypes;
	}
	public void setAppointmentTypes(String appointmentTypes) {
		this.appointmentTypes = appointmentTypes;
	}
	public String getAppointmentPeriod() {
		return appointmentPeriod;
	}
	public void setAppointmentPeriod(String appointmentPeriod) {
		this.appointmentPeriod = appointmentPeriod;
	}
	public String getPrimaryDepartment() {
		return primaryDepartment;
	}
	public void setPrimaryDepartment(String primaryDepartment) {
		this.primaryDepartment = primaryDepartment;
	}
	public String getOtherDepartment() {
		return otherDepartment;
	}
	public void setOtherDepartment(String otherDepartment) {
		this.otherDepartment = otherDepartment;
	}
	public String getPrimaryAffiliation() {
		return primaryAffiliation;
	}
	public void setPrimaryAffiliation(String primaryAffiliation) {
		this.primaryAffiliation = primaryAffiliation;
	}
	public int getHarvesterFlag() {
		return harvesterFlag;
	}
	public void setHarvesterFlag(int harvesterFlag) {
		this.harvesterFlag = harvesterFlag;
	}
	public LocalDateTime getCreateDate() {
		return createDate;
	}
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmailOther() {
		return emailOther;
	}
	public void setEmailOther(String emailOther) {
		this.emailOther = emailOther;
	}
}
