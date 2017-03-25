package reciter.model.identity;

public class Grant {
	private String grantId;
	private String sponsorAwardId;
	private String department;
	private String organization;
	
	public Grant() {}
	
	public String getGrantId() {
		return grantId;
	}
	public void setGrantId(String grantId) {
		this.grantId = grantId;
	}
	public String getSponsorAwardId() {
		return sponsorAwardId;
	}
	public void setSponsorAwardId(String sponsorAwardId) {
		this.sponsorAwardId = sponsorAwardId;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
}
