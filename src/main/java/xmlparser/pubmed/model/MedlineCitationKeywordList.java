package xmlparser.pubmed.model;

import java.util.List;

public class MedlineCitationKeywordList {
	private enum KeywordListOwner {
		NASA,
		PIP,
		KIE,
		NLM,
		NOTNLM,
		HHS
	}
	private KeywordListOwner keywordListOwner;
	private List<MedlineCitationKeyword> keywordList;
	
	public List<MedlineCitationKeyword> getKeywordList() {
		return keywordList;
	}
	public void setKeywordList(List<MedlineCitationKeyword> keywordList) {
		this.keywordList = keywordList;
	}
}
