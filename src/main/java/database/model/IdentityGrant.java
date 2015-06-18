package database.model;

/**
 * rc_identity_grant table.
 * @author jil3004
 *
 */
public class IdentityGrant {

	private long id;
	private String cwid;
	private String date;
	private String grandid;
	private String sponsorAwardId;
	private String administeringDepartmentDivision;
	private String awardingOrganization;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getGrandid() {
		return grandid;
	}
	public void setGrandid(String grandid) {
		this.grandid = grandid;
	}
	public String getSponsorAwardId() {
		return sponsorAwardId;
	}
	public void setSponsorAwardId(String sponsorAwardId) {
		this.sponsorAwardId = sponsorAwardId;
	}
	public String getAdministeringDepartmentDivision() {
		return administeringDepartmentDivision;
	}
	public void setAdministeringDepartmentDivision(
			String administeringDepartmentDivision) {
		this.administeringDepartmentDivision = administeringDepartmentDivision;
	}
	public String getAwardingOrganization() {
		return awardingOrganization;
	}
	public void setAwardingOrganization(String awardingOrganization) {
		this.awardingOrganization = awardingOrganization;
	}
}
