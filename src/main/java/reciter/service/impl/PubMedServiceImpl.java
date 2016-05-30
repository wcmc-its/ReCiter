package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import reciter.database.mongo.PubMedDataAccess;
import reciter.service.PubMedService;
import reciter.xml.parser.pubmed.model.PubmedArticle;

@Service
public class PubMedServiceImpl implements PubMedService {

	@Autowired
	private PubMedDataAccess pubMedDataAccess;
	
	@Override
	public void insertPubMedArticle(PubmedArticle article) {
		String json = new Gson().toJson(article);
		pubMedDataAccess.insertPubMedArticle(json);
	}

	@Override
	public void upsertPubMedArticle(PubmedArticle article) {
		String pmid = article.getMedlineCitation().getPmid().getPmidString();
		String json = new Gson().toJson(article);
	}
	
	@Override
	public List<PubmedArticle> retrieve(String cwid) {
		throw new RuntimeException("Implement me.");
	}
	
	public PubMedDataAccess getPubMedDataAccess() {
		return pubMedDataAccess;
	}
	
	public void setPubMedDataAccess(PubMedDataAccess pubMedDataAccess) {
		this.pubMedDataAccess = pubMedDataAccess; 
	}
}
