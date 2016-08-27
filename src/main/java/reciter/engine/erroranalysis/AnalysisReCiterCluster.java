package reciter.engine.erroranalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class AnalysisReCiterCluster {

	public Map<String, Integer> getTargetAuthorNameCounts(List<ReCiterArticle> list, TargetAuthor targetAuthor) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (ReCiterArticle reCiterArticle : list) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				if (author.getAuthorName().getLastName().equalsIgnoreCase(targetAuthor.getAuthorName().getLastName())) {
					String fullName = author.getAuthorName().getLastName() + " " + 
							author.getAuthorName().getMiddleName() + " " + 
							author.getAuthorName().getFirstName();
					if (map.containsKey(fullName)) {
						int count = map.get(fullName);
						map.put(fullName, ++count);
					} else {
						map.put(fullName, 1);
					}
				}
			}
		}
		return map;
	}
}
