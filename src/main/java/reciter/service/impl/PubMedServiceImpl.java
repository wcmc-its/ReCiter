package reciter.service.impl;

import java.util.List;

import com.google.gson.Gson;

import reciter.database.mongo.PubMedServiceDao;
import reciter.database.mongo.impl.PubMedServiceDaoImpl;
import reciter.service.PubMedService;
import reciter.xml.parser.pubmed.model.PubmedArticle;

public class PubMedServiceImpl implements PubMedService {

	@Override
	public void persist(PubmedArticle article) {
		String json = new Gson().toJson(article);
		PubMedServiceDao pubmedServiceDao = new PubMedServiceDaoImpl();
		pubmedServiceDao.insertPubMedArticle(json);
	}

	@Override
	public List<PubmedArticle> retrieve(String cwid) {
		throw new RuntimeException("Implement me.");
	}
	
	
}
