package reciter.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.model.scopus.ScopusArticle;
import reciter.service.ScopusService;

public class ScopusController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusController.class);

	@Autowired
	private ScopusService scopusService;
	
	@RequestMapping(value = "/reciter/save/scopus/articles/", method = RequestMethod.PUT)
	@ResponseBody
	public void savePubMedArticles(@RequestBody List<ScopusArticle> scopusArticles) {
		slf4jLogger.info("calling savePubMedArticles with number of Scopus articles=" + scopusArticles.size());
		scopusService.save(scopusArticles);
	}
	
	@RequestMapping(value = "/reciter/find/scopus/articles/pmids/", method = RequestMethod.GET)
	@ResponseBody
	public List<ScopusArticle> findByPmids(@RequestParam List<Long> pmids) {
		slf4jLogger.info("calling findByPmids with size of pmids=" + pmids);
		return scopusService.findByPmids(pmids);
	}
}
