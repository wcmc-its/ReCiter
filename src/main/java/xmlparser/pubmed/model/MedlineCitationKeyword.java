package xmlparser.pubmed.model;

public class MedlineCitationKeyword {

	private MedlineCitationYNEnum majorTopicYN;
	private String keyword;
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
