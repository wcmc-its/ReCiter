package reciter.model.article;

public class ReCiterMeshHeadingDescriptorName {

	private ReCiterCitationYNEnum majorTopicYN;
	private String descriptorName;
	
	public ReCiterCitationYNEnum getMajorTopicYN() {
		return majorTopicYN;
	}
	public void setMajorTopicYN(ReCiterCitationYNEnum majorTopicYN) {
		this.majorTopicYN = majorTopicYN;
	}
	public String getDescriptorName() {
		return descriptorName;
	}
	public void setDescriptorName(String descriptorName) {
		this.descriptorName = descriptorName;
	}
}
