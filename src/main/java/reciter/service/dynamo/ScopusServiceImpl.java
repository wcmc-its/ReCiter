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
            reciter.database.dynamodb.model.ScopusArticle dbScopusArticle = null;
            /* Commented out to store pubmed id as unique ID for ScopusArticle table in DynamoDb
             * if (scopusArticle.getPubmedId() == 0) {
                dbScopusArticle = new reciter.database.dynamodb.model.ScopusArticle(
                        String.valueOf(scopusArticle.getPubmedId()),
                        scopusArticle
                );
            } else {
                dbScopusArticle = new reciter.database.dynamodb.model.ScopusArticle(
                        scopusArticle.getDoi().trim(),
                        scopusArticle
                ); 
            }*/

            if (scopusArticle.getPubmedId() != 0) {
                dbScopusArticle = new reciter.database.dynamodb.model.ScopusArticle(
                        String.valueOf(scopusArticle.getPubmedId()),
                        scopusArticle
                );
            }
            if (dbScopusArticle != null)
                dbScopusArticles.add(dbScopusArticle);
        }
        //scopusRepository.saveAll(dbScopusArticles);
        scopusRepository.save(dbScopusArticles);
    }

    @Override
    public List<ScopusArticle> findByPmids(List<String> pmids) {
        List<ScopusArticle> scopusArticles = null;
        //Iterator<reciter.database.dynamodb.model.ScopusArticle> iterator = scopusRepository.findAllById(pmids).iterator();
        try {
	        Iterator<reciter.database.dynamodb.model.ScopusArticle> iterator = scopusRepository.findAll(pmids).iterator();
	        scopusArticles = new ArrayList<>(pmids.size());
	        while (iterator.hasNext()) {
	            scopusArticles.add(iterator.next().getScopusArticle());
	        }
        } catch(NullPointerException ne) {	
    		return null;	
    	}
        return scopusArticles;
    }

    @Override
    public ScopusArticle findByPmid(String pmid) {
        /*reciter.database.dynamodb.model.ScopusArticle scopusArticle = scopusRepository.findById(pmid).orElseGet(() -> null);
        if (scopusArticle != null) {
            return scopusArticle.getScopusArticle();
        }
        return null;*/
    	return scopusRepository.findOne(pmid).getScopusArticle();
    }

    @Override
    public void delete() {
        scopusRepository.deleteAll();
    }
}
