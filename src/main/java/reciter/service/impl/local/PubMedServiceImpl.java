package reciter.service.impl.local;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.repository.PubMedRepository;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

@Service("pubMedService")
public class PubMedServiceImpl implements PubMedService {

	@Autowired
	private PubMedRepository pubMedRepository;
	
	@Override
	public void save(List<PubMedArticle> pubMedArticles) {
		pubMedRepository.save(pubMedArticles);
	}

	@Override
	public List<PubMedArticle> findByPmids(List<Long> pmids) {
		// TODO Auto-generated method stub
		return null;
	}
}
