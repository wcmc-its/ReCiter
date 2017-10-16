package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.repository.ScopusArticleRepository;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ScopusService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service("scopusService")
public class ScopusServiceImpl implements ScopusService {

    @Autowired
    private ScopusArticleRepository scopusRepository;

    @Override
    public void save(Collection<ScopusArticle> scopusArticles) {
        List<reciter.database.dynamodb.model.ScopusArticle> dbScopusArticles = new ArrayList<>();
        for (ScopusArticle scopusArticle : scopusArticles) {
            reciter.database.dynamodb.model.ScopusArticle dbScopusArticle = new reciter.database.dynamodb.model.ScopusArticle(
                    scopusArticle.getPubmedId(),
                    scopusArticle
            );
            dbScopusArticles.add(dbScopusArticle);
            System.out.println("Saving Scopus article:" + scopusArticle.getPubmedId());
        }
        scopusRepository.save(dbScopusArticles.get(0));
    }

    @Override
    public List<ScopusArticle> findByPmids(List<Long> pmids) {
        Iterator<reciter.database.dynamodb.model.ScopusArticle> iterator = scopusRepository.findAll(pmids).iterator();
        List<ScopusArticle> scopusArticles = new ArrayList<>(pmids.size());
        while (iterator.hasNext()) {
            scopusArticles.add(iterator.next().getScopusArticle());
        }
        return scopusArticles;
    }
}
