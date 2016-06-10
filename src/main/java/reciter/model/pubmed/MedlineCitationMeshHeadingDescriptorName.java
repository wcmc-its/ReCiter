package reciter.model.pubmed;

public class MedlineCitationMeshHeadingDescriptorName {
	
	private MedlineCitationYNEnum majorTopicYN;
	private String descriptorName;
	private String descriptorNameUI;
	
	public String getDescriptorNameString() {
		return descriptorName;
	}
	public void setDescriptorName(String descriptorName) {
		this.descriptorName = descriptorName;
	}
	public MedlineCitationYNEnum getMajorTopicYN() {
		return majorTopicYN;
	}
	public void setMajorTopicYN(MedlineCitationYNEnum majorTopicYN) {
		this.majorTopicYN = majorTopicYN;
	}
	public String getDescriptorNameUI() {
		return descriptorNameUI;
	}
	public void setDescriptorNameUI(String descriptorNameUI) {
		this.descriptorNameUI = descriptorNameUI;
	}

}
