package reciter.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

@Controller
public class PubMedController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(PubMedController.class);

	@Autowired
	private PubMedService pubMedService;
	
	@RequestMapping(value = "/reciter/save/pubmed/articles/", method = RequestMethod.PUT)
	@ResponseBody
	public void savePubMedArticles(@RequestBody List<PubMedArticle> pubMedArticles) {
		slf4jLogger.info("calling savePubMedArticles with numberOfPubmedArticles=" + pubMedArticles.size());
		pubMedService.save(pubMedArticles);
	}
	
	@RequestMapping(value = "/reciter/find/pubmed/articles/pmids/", method = RequestMethod.GET)
	@ResponseBody
	public List<PubMedArticle> findByPmids(@RequestParam List<Long> pmids) {
		slf4jLogger.info("calling findByPmids with size of pmids=" + pmids);
		return pubMedService.findByPmids(pmids);
	}
}
