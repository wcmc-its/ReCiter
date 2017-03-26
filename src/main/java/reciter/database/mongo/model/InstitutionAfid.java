package reciter.database.mongo.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="institutionafid")
public class InstitutionAfid {

	@Id
	private ObjectId id;
	
	private String institution;
	private int afid;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getInstitution() {
		return institution;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	public int getAfid() {
		return afid;
	}
	public void setAfid(int afid) {
		this.afid = afid;
	}
	
	
}
