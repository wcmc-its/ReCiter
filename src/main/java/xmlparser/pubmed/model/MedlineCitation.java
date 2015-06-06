package xmlparser.pubmed.model;

import java.util.List;

public class MedlineCitation {
	
	private enum MedlineCitationOwner {
		NLM,
		NASA,
		PIP,
		KIE,
		HSR,
		HMD,
		SIS,
		NOTNLM
	}
	
	private enum MedlineCitationStatus {
		COMPLETED,
		IN_PROCESS,
		PUBMED_NOT_MEDLINE,
		IN_DATA_REVIEW,
		PUBLISHER,
		MEDLINE,
		OLDMEDLINE
	}
	
	private MedlineCitationPMID pmid;
	private MedlineCitationOwner medlineCitationOwner;
	private MedlineCitationStatus medlineCitationStatus;
	private MedlineCitationVersionDate medlineCitationVersionDate;
	private MedlineCitationVersionID medlineCitationVersionID;
	
	private MedlineCitationDate dateCreated;
	private MedlineCitationDate dateCompleted;
	private MedlineCitationDate dateRevised;
	
	private MedlineCitationArticle article;
	private List<MedlineCitationMeshHeading> meshHeadingList;
	private MedlineCitationKeywordList keywordList;
	
	public MedlineCitationPMID getPmid() {
		return pmid;
	}
	public void setPmid(MedlineCitationPMID pmid) {
		this.pmid = pmid;
	}
	public MedlineCitationArticle getArticle() {
		return article;
	}
	public void setArticle(MedlineCitationArticle article) {
		this.article = article;
	}
	public List<MedlineCitationMeshHeading> getMeshHeadingList() {
		return meshHeadingList;
	}
	public void setMeshHeadingList(List<MedlineCitationMeshHeading> meshHeadingList) {
		this.meshHeadingList = meshHeadingList;
	}
	public MedlineCitationKeywordList getKeywordList() {
		return keywordList;
	}
	public void setKeywordList(MedlineCitationKeywordList keywordList) {
		this.keywordList = keywordList;
	}
	public MedlineCitationDate getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(MedlineCitationDate dateCreated) {
		this.dateCreated = dateCreated;
	}
	public MedlineCitationDate getDateCompleted() {
		return dateCompleted;
	}
	public void setDateCompleted(MedlineCitationDate dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	public MedlineCitationDate getDateRevised() {
		return dateRevised;
	}
	public void setDateRevised(MedlineCitationDate dateRevised) {
		this.dateRevised = dateRevised;
	}
}
