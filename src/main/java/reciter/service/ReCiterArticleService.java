package reciter.service;

import java.util.List;

import reciter.model.article.ReCiterArticle;

public interface ReCiterArticleService {

	List<ReCiterArticle> retrieve(String cwid);
}
