package reciter.database.model;

import java.util.List;

public class IdentityAlternateDeptNames {

	private long id;
	private String nameOfa;
	private List<String> alternateNames;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNameOfa() {
		return nameOfa;
	}
	public void setNameOfa(String nameOfa) {
		this.nameOfa = nameOfa;
	}
	public List<String> getAlternateNames() {
		return alternateNames;
	}
	public void setAlternateNames(List<String> alternateNames) {
		this.alternateNames = alternateNames;
	}
}
