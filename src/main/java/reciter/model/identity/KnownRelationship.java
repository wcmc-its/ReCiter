package reciter.model.identity;

public class KnownRelationship {

	private String cwid;
	private AuthorName name;
	private String type;
	
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public AuthorName getName() {
		return name;
	}
	public void setName(AuthorName name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
