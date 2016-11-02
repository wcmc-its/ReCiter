package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.repository.ScopusRepository;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ScopusService;

@Service("scopusService")
public class ScopusServiceImpl implements ScopusService {

	@Autowired
	private ScopusRepository scopusRepository;
	
	@Override
	public void save(List<ScopusArticle> scopusArticles) {
		scopusRepository.save(scopusArticles);
	}
	
	@Override
	public List<ScopusArticle> findByPubmedId(List<Long> pmids) {
		return scopusRepository.findByPubmedId(pmids);
	}
}
