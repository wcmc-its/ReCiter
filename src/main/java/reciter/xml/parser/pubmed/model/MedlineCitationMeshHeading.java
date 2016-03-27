package reciter.xml.parser.pubmed.model;

import java.util.List;

public class MedlineCitationMeshHeading {

	private MedlineCitationMeshHeadingDescriptorName descriptorName;
	private List<MedlineCitationMeshHeadingQualifierName> qualifierNameList;
	
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
