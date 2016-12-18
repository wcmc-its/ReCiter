package reciter.model.article;

import java.util.List;

public class ReCiterArticleMeshHeading {
	private ReCiterMeshHeadingDescriptorName descriptorName;
	private List<ReCiterMeshHeadingQualifierName> qualifierNameList;
	
	public ReCiterMeshHeadingDescriptorName getDescriptorName() {
		return descriptorName;
	}
	public void setDescriptorName(ReCiterMeshHeadingDescriptorName descriptorName) {
		this.descriptorName = descriptorName;
	}
	public List<ReCiterMeshHeadingQualifierName> getQualifierNameList() {
		return qualifierNameList;
	}
	public void setQualifierNameList(List<ReCiterMeshHeadingQualifierName> qualifierNameList) {
		this.qualifierNameList = qualifierNameList;
	}
	
}
