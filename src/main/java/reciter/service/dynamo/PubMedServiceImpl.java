package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.repository.PubMedArticleRepository;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

@Slf4j
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
        try{
        	pubMedRepository.saveAll(pubmedArticlesDb);
        } catch(Exception e) { //This is to skip over articles with huge list of authors e.g. yiwang - 29547300
        	log.info(e.getMessage());
        }
    }

    @Override
    public List<PubMedArticle> findByPmids(List<Long> pmids) {
        List<PubMedArticle> pubMedArticles = null;
        Iterator<reciter.database.dynamodb.model.PubMedArticle> iterator = pubMedRepository.findAllById(pmids).iterator();
        pubMedArticles = new ArrayList<>(pmids.size());
        while (iterator.hasNext()) {
            pubMedArticles.add(iterator.next().getPubMedArticle());
        }
        return pubMedArticles;
    }

    @Override
    public PubMedArticle findByPmid(Long pmid) {
        reciter.database.dynamodb.model.PubMedArticle pubMedArticle = pubMedRepository.findById(pmid).orElseGet(() -> null);
        if (pubMedArticle != null) {
            return pubMedArticle.getPubMedArticle();
        }
        return null;
    }
}
