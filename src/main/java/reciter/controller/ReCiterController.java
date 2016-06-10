package reciter.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.BoardCertificationService;
import reciter.service.PubMedService;
import reciter.service.TargetAuthorService;
import reciter.xml.retriever.engine.DefaultReCiterRetrievalEngine;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
@EnableAutoConfiguration
public class ReCiterController {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);
	
	@Autowired
	private BoardCertificationService boardCertificationService;
	
	@Autowired
	private TargetAuthorService targetAuthorService;
	
	@Autowired
	private PubMedService pubMedService;
	
	@RequestMapping("/")
	@ResponseBody
	public List<String> home() {
		List<String> list = boardCertificationService.getBoardCertificationsByCwid("ccole");
		return list;
	}

	@RequestMapping(value = "/reciter/targetauthor/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public TargetAuthor getTargetAuthorByCwid(@RequestParam(value="cwid") String cwid) {
		return targetAuthorService.getTargetAuthor(cwid);
	}
	
	@RequestMapping(value = "/reciter/pubmedarticle/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<Long> getPubMedArticleByCwid(@RequestParam(value="cwid") String cwid) {
		ReCiterRetrievalEngine retrievalEngine = new DefaultReCiterRetrievalEngine();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor("aas2004");
		List<PubMedArticle> pubMedArticles = new ArrayList<PubMedArticle>();
		try {
			pubMedArticles = retrievalEngine.retrieve(targetAuthor);
		} catch (IOException e) {
			slf4jLogger.error("Error retrieving articles for cwid=[" + cwid + "].", e);
		}
		List<Long> pmids = new ArrayList<Long>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
		}
		pubMedService.save(pubMedArticles);
		return pmids;
	}
}