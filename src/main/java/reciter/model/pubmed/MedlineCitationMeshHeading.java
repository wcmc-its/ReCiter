package reciter.model.pubmed;

import java.util.List;

public class MedlineCitationMeshHeading {

	private MedlineCitationMeshHeadingDescriptorName descriptorName;
	private List<MedlineCitationMeshHeadingQualifierName> qualifierNameList;
	
	public MedlineCitationMeshHeading() {}
	
	public MedlineCitationMeshHeadingDescriptorName getDescriptorName() {
		return descriptorName;
	}
	public void setDescriptorName(MedlineCitationMeshHeadingDescriptorName descriptorName) {
		this.descriptorName = descriptorName;
	}
	public List<MedlineCitationMeshHeadingQualifierName> getQualifierNameList() {
		return qualifierNameList;
	}
	public void setQualifierNameList(List<MedlineCitationMeshHeadingQualifierName> qualifierNameList) {
		this.qualifierNameList = qualifierNameList;
	}
	
}
