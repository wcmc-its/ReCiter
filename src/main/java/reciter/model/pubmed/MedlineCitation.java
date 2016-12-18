package reciter.model.pubmed;

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
	
	public MedlineCitation() {}
	
	private MedlineCitationPMID medlineCitationPMID;
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
	private List<MedlineCitationCommentsCorrections> commentsCorrectionsList;
	
	public MedlineCitationPMID getMedlineCitationPMID() {
		return medlineCitationPMID;
	}
	public void setMedlineCitationPMID(MedlineCitationPMID medlineCitationPMID) {
		this.medlineCitationPMID = medlineCitationPMID;
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
	public List<MedlineCitationCommentsCorrections> getCommentsCorrectionsList() {
		return commentsCorrectionsList;
	}
	public void setCommentsCorrectionsList(List<MedlineCitationCommentsCorrections> commentsCorrectionsList) {
		this.commentsCorrectionsList = commentsCorrectionsList;
	}
}
