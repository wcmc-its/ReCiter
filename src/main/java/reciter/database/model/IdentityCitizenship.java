package reciter.database.model;

/**
 * Model class for table rc_identity_citizenship.
 * @author jil3004
 *
 */
public class IdentityCitizenship {
	
	private long id;
	private String cwid;
	private String country;
	
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
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
}
