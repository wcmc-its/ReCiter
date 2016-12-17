package reciter.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.PubMedArticleMongo;
import reciter.database.mongo.repository.PubMedRepository;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

@Service("pubMedService")
public class PubMedServiceImpl implements PubMedService {

	@Autowired
	private PubMedRepository pubMedRepository;

	@Override
	public void save(Collection<PubMedArticle> pubMedArticles) {
		List<PubMedArticleMongo> pubMedArticleMongos = new ArrayList<>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			PubMedArticleMongo pubMedArticleMongo = new PubMedArticleMongo();
			pubMedArticleMongo.setId(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
			pubMedArticleMongo.setPubMedArticle(pubMedArticle);
			pubMedArticleMongos.add(pubMedArticleMongo);
		}
		pubMedRepository.save(pubMedArticleMongos);
	}

	@Override
	public List<PubMedArticle> findByPmids(List<Long> pmids) {
		Iterator<PubMedArticleMongo> iterator = pubMedRepository.findAll(pmids).iterator();
		List<PubMedArticle> pubMedArticles = new ArrayList<>(pmids.size());
		while (iterator.hasNext()) {
			pubMedArticles.add(iterator.next().getPubMedArticle());
		}
		return pubMedArticles;
	}
}
