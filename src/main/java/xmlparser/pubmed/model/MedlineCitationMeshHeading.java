package xmlparser.pubmed.model;

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
}
