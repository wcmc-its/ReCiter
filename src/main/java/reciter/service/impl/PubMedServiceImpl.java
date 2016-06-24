package reciter.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import reciter.database.mongo.repository.PubMedRepository;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

@Service("pubMedService")
public class PubMedServiceImpl implements PubMedService {

	@Autowired
	private PubMedRepository pubMedRepository;
	
	@Override
	public void insertPubMedArticle(PubMedArticle article) {
		String json = new Gson().toJson(article);
//		PubMedRepository pubMedRepository = new PubMedDataAccessImpl();
//		pubMedRepository.insertPubMedArticle(json);
	}

	@Override
	public void upsertPubMedArticle(PubMedArticle article) {
//		long pmid = article.getMedlineCitation().getPmid().getPmid();
//		String json = new Gson().toJson(article);
//		PubMedDataAccess pubMedDataAccess = new PubMedDataAccessImpl();
//		pubMedDataAccess.upsertPubMedArticle(json, pmid);
	}
	
	@Override
	public void save(List<PubMedArticle> pubMedArticles) {		
		pubMedRepository.save(pubMedArticles);
	}

	@Override
	public List<PubMedArticle> retrieve(String cwid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PubMedArticle> findByMedlineCitationMedlineCitationPMIDPmid(List<Long> pmids) {
		return pubMedRepository.findByMedlineCitationMedlineCitationPMIDPmid(pmids);
	}
	
	@Override
	public List<PubMedArticle> findMatchingAuthorsByLastName(String lastName) {
		return pubMedRepository.findMatchingAuthorsByLastName(lastName);
	}
}
