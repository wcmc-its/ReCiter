package reciter.database.mongo.model;

public class Institution {
	private String name;
	private int degreeYear;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDegreeYear() {
		return degreeYear;
	}
	public void setDegreeYear(int degreeYear) {
		this.degreeYear = degreeYear;
	}
}
