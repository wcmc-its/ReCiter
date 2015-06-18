package database.model;

/**
 * Represents the table rc_identity_degree
 * 
 * @author jil3004
 *
 */
public class IdentityDegree {

	private int id;
	private String cwid;
	private int bachelor;
	private int masters;
	private int doctoral;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public int getBachelor() {
		return bachelor;
	}
	public void setBachelor(int bachelor) {
		this.bachelor = bachelor;
	}
	public int getMasters() {
		return masters;
	}
	public void setMasters(int masters) {
		this.masters = masters;
	}
	public int getDoctoral() {
		return doctoral;
	}
	public void setDoctoral(int doctoral) {
		this.doctoral = doctoral;
	}
	
	
}
