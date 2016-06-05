package reciter.service.impl;

import java.util.List;

import com.google.gson.Gson;

import reciter.database.mongo.PubMedDataAccess;
import reciter.database.mongo.impl.PubMedDataAccessImpl;
import reciter.service.PubMedService;
import reciter.xml.parser.pubmed.model.PubMedArticle;

public class PubMedServiceImpl implements PubMedService {

	@Override
	public void insertPubMedArticle(PubMedArticle article) {
		String json = new Gson().toJson(article);
		PubMedDataAccess pubMedDataAccess = new PubMedDataAccessImpl();
		pubMedDataAccess.insertPubMedArticle(json);
	}

	@Override
	public void upsertPubMedArticle(PubMedArticle article) {
		long pmid = article.getMedlineCitation().getPmid().getPmid();
		String json = new Gson().toJson(article);
		PubMedDataAccess pubMedDataAccess = new PubMedDataAccessImpl();
		pubMedDataAccess.upsertPubMedArticle(json, pmid);
	}
	
	@Override
	public List<PubMedArticle> retrieve(String cwid) {
		throw new RuntimeException("Implement me.");
	}
}
