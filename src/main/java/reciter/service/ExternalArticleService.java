package reciter.service;

import java.util.List;

import reciter.database.dynamodb.model.ExternalArticle;

public interface ExternalArticleService {

    void save(ExternalArticle externalArticle);

    List<ExternalArticle> findByUid(String uid);

    ExternalArticle find(String uid, String articleId);

    void delete(String uid, String articleId);
}
