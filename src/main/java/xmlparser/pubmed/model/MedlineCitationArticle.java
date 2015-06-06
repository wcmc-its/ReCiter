package xmlparser.pubmed.model;

import java.util.List;

public class MedlineCitationArticle {

	private enum PubModel {
		PRINT,
		PRINT_ELECTRONIC,
		ELECTRONIC,
		ELECTRONIC_PRINT,
		ELECTRONIC_ECOLLECTION
	};
	
	private PubModel pubModel;
	private MedlineCitationJournal journal;
	private String articleTitle;
	private String pagination;
	private MedlineCitationArticleELocationID eLocationID;
	private MedlineCitationYNEnum authorListCompleteYN;
	private List<MedlineCitationArticleAuthor> authorList;
	private MedlineCitationYNEnum grantListCompleteYN;
	private List<MedlineCitationPublicationType> publicationTypeList;
	private MedlineCitationDate articleDate;
	private MedlineCitationJournalInfo journalInfo;
	private List<MedlineCitationChemical> chemicalList;
	private List<MedlineCitationMeshHeading> meshHeadingList;
	private List<MedlineCitationSubset> citationSubsetList;
	
	public String getArticleTitle() {
		return articleTitle;
	}
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	public List<MedlineCitationArticleAuthor> getAuthorList() {
		return authorList;
	}
	public void setAuthorList(List<MedlineCitationArticleAuthor> authorList) {
		this.authorList = authorList;
	}
	public MedlineCitationJournal getJournal() {
		return journal;
	}
	public void setJournal(MedlineCitationJournal journal) {
		this.journal = journal;
	}
}
