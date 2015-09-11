package reciter.model.author;

import java.util.List;

import database.model.IdentityDirectory;

/**
 * @author jil3004
 *
 */
public class TargetAuthor extends ReCiterAuthor {

	private String cwid;
	private String department;
	private String otherDeparment;
	private AuthorEducation education;
	private List<IdentityDirectory> aliasList;
	
	/**
	 * @return the aliasList
	 */
	public List<IdentityDirectory> getAliasList() {
		return aliasList;
	}

	/**
	 * @param aliasList the aliasList to set
	 */
	public void setAliasList(List<IdentityDirectory> aliasList) {
		this.aliasList = aliasList;
	}

	public TargetAuthor(AuthorName name, AuthorAffiliation affiliation) {
		super(name, affiliation);
	}

	public String getCwid() {
		return cwid;
	}

	public void setCwid(String cwid) {
		this.cwid = cwid;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public AuthorEducation getEducation() {
		return education;
	}

	public void setEducation(AuthorEducation education) {
		this.education = education;
	}

	public String getOtherDeparment() {
		return otherDeparment;
	}

	public void setOtherDeparment(String otherDeparment) {
		this.otherDeparment = otherDeparment;
	}

}
