package reciter.model.pubmed;

public class MedlineCitationMeshHeadingQualifierName {
	private MedlineCitationYNEnum majorTopicYN;
	private String qualifierNameUI;
	private String qualifierName;
	
	public MedlineCitationMeshHeadingQualifierName() {}
	
	public MedlineCitationYNEnum getMajorTopicYN() {
		return majorTopicYN;
	}
	public void setMajorTopicYN(MedlineCitationYNEnum majorTopicYN) {
		this.majorTopicYN = majorTopicYN;
	}
	public String getQualifierNameUI() {
		return qualifierNameUI;
	}
	public void setQualifierNameUI(String qualifierNameUI) {
		this.qualifierNameUI = qualifierNameUI;
	}
	public String getQualifierName() {
		return qualifierName;
	}
	public void setQualifierName(String qualifierName) {
		this.qualifierName = qualifierName;
	}
}
