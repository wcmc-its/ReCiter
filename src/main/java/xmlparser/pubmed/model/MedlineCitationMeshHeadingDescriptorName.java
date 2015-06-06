package xmlparser.pubmed.model;

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

}
