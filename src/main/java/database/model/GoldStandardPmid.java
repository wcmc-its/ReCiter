package database.model;

public class GoldStandardPmid {
	int id;
	String cwid;
	String pmid;
	String rejected;
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the cwid
	 */
	public String getCwid() {
		return cwid;
	}
	/**
	 * @param cwid the cwid to set
	 */
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	/**
	 * @return the pmid
	 */
	public String getPmid() {
		return pmid;
	}
	/**
	 * @param pmid the pmid to set
	 */
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
	/**
	 * @return the rejected
	 */
	public String getRejected() {
		return rejected;
	}
	/**
	 * @param rejected the rejected to set
	 */
	public void setRejected(String rejected) {
		this.rejected = rejected;
	}	
}
