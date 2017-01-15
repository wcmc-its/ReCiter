package reciter.model.identity;

public class KnownRelationship {

	private String uid;
	private AuthorName name;
	private String type;
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
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
	@Override
	public String toString() {
		return "KnownRelationship [uid=" + uid + ", name=" + name + ", type=" + type + "]";
	}
}
