package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.repository.PubMedArticleRepository;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service("pubMedService")
public class PubMedServiceImpl implements PubMedService {

    @Autowired
    private PubMedArticleRepository pubMedRepository;

    @Override
    public void save(Collection<PubMedArticle> pubMedArticles) {
        List<reciter.database.dynamodb.model.PubMedArticle> pubmedArticlesDb = new ArrayList<>();
        for (PubMedArticle pubMedArticle : pubMedArticles) {
            reciter.database.dynamodb.model.PubMedArticle pubMedArticleDb = new reciter.database.dynamodb.model.PubMedArticle(
                    pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid(),
                    pubMedArticle
            );
            pubmedArticlesDb.add(pubMedArticleDb);
        }
        pubMedRepository.saveAll(pubmedArticlesDb);
    }

    @Override
    public List<PubMedArticle> findByPmids(List<Long> pmids) {
        List<PubMedArticle> pubMedArticles = null;
        Iterator<reciter.database.dynamodb.model.PubMedArticle> iterator = pubMedRepository.findAllById(pmids).iterator();
        pubMedArticles = new ArrayList<>(pmids.size());
        while (iterator.hasNext()) {
            pubMedArticles.add(iterator.next().getPubmedArticle());
        }
        return pubMedArticles;
    }

    @Override
    public PubMedArticle findByPmid(Long pmid) {
        reciter.database.dynamodb.model.PubMedArticle pubMedArticle = pubMedRepository.findById(pmid).orElseGet(() -> null);
        if (pubMedArticle != null) {
            return pubMedArticle.getPubmedArticle();
        }
        return null;
    }
}
