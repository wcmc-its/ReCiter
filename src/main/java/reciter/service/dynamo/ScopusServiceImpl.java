package reciter.service.dynamo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.repository.ScopusArticleRepository;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ScopusService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service("scopusService")
public class ScopusServiceImpl implements ScopusService {

    @Autowired
    private ScopusArticleRepository scopusRepository;

    @Override
    public void save(Collection<ScopusArticle> scopusArticles) {
        List<reciter.database.dynamodb.model.ScopusArticle> dbScopusArticles = new ArrayList<>();
        for (ScopusArticle scopusArticle : scopusArticles) {
            reciter.database.dynamodb.model.ScopusArticle dbScopusArticle;
            if (scopusArticle.getPubmedId() == 0) {
                dbScopusArticle = new reciter.database.dynamodb.model.ScopusArticle(
                        String.valueOf(scopusArticle.getPubmedId()),
                        scopusArticle
                );
            } else {
                dbScopusArticle = new reciter.database.dynamodb.model.ScopusArticle(
                        scopusArticle.getDoi(),
                        scopusArticle
                );
            }
            dbScopusArticles.add(dbScopusArticle);
        }
        log.info("Saving number of scopus articles: {},", scopusArticles.size());
        scopusRepository.save(dbScopusArticles);
    }

    @Override
    public List<ScopusArticle> findByPmids(List<String> pmids) {
        Iterator<reciter.database.dynamodb.model.ScopusArticle> iterator = scopusRepository.findAll(pmids).iterator();
        List<ScopusArticle> scopusArticles = new ArrayList<>(pmids.size());
        while (iterator.hasNext()) {
            scopusArticles.add(iterator.next().getScopusArticle());
        }
        return scopusArticles;
    }
}
