package reciter.database.mongo.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "meshterm")
public class MeshTerm {

	private String mesh;
	private long count;
	
	public String getMesh() {
		return mesh;
	}
	public void setMesh(String mesh) {
		this.mesh = mesh;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
}
