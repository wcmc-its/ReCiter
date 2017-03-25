package reciter.model.article;

import java.util.ArrayList;
import java.util.List;

public class ReCiterArticleKeywords {
	
	public class Keyword {
		private String keyword;
		Keyword() {}
		Keyword(String keyword) {
			this.keyword = keyword;
		}
		public String getKeyword() {
			return keyword;
		}
		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}
	}
	private List<Keyword> keywords;
	
	public ReCiterArticleKeywords() {
		keywords = new ArrayList<Keyword>();
	}
	
	public boolean exist() {
		return keywords != null;
	}
	
	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}
	
	public void addKeyword(String keyword) {
		keywords.add(new Keyword(keyword));
	}
	
	public boolean isKeywordExist(String keywordStr){
		if(exist()){
			for(Keyword k: keywords){
				if(k.getKeyword().equalsIgnoreCase(keywordStr))return true;
			}
		}
		return false;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Keyword keyword : keywords) {
			sb.append(keyword.getKeyword());
			sb.append(",");
		}
		return sb.toString();
	}
}
