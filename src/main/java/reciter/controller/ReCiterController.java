package reciter.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.database.mongo.model.ESearchResult;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.BoardCertificationService;
import reciter.service.ESearchResultService;
import reciter.service.PubMedService;
import reciter.service.TargetAuthorService;
import reciter.xml.retriever.engine.DefaultReCiterRetrievalEngine;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
@EnableAutoConfiguration
public class ReCiterController {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);
	
	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;

	@Autowired
	private BoardCertificationService boardCertificationService;
	
	@Autowired
	private TargetAuthorService targetAuthorService;
	
	@Autowired
	private PubMedService pubMedService;
	
	@Autowired
	private ESearchResultService eSearchResultService;
	
	@Autowired
	private ReCiterRetrievalEngine defaultReCiterRetrievalEngine;
	
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
	
	@RequestMapping(value = "/reciter/test", method = RequestMethod.GET)
	@ResponseBody
	public List<PubMedArticle> findByMedlineCitationMedlineCitationPMIDPmid() {
//		List<Long> pmids = new ArrayList<Long>();
//		pmids.add(27278579L);
//		pmids.add(1L);
//		pmids.add(272779L);
//		pmids.add(27250862L);
//		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(pmids);
//		return pubMedArticles;
		ReCiterRetrievalEngine retrievalEngine = new DefaultReCiterRetrievalEngine();
		List<PubMedArticle> pubMedArticles = null;
		try {
			pubMedArticles = retrievalEngine.retrieve(null);
		} catch (IOException e) {
			slf4jLogger.error("Error retrieving articles for cwid=[" + "" + "].", e);
		}
		
		return null;
	}
	
	@RequestMapping(value = "/reciter/pubmedarticle/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<Long> getPubMedArticleByCwid(@RequestParam(value="cwid") String cwid) {
		// Get target author information.
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		
		// Retrieve the articles.
		List<PubMedArticle> pubMedArticles = null;
		try {
			pubMedArticles = defaultReCiterRetrievalEngine.retrieve(targetAuthor);
		} catch (IOException e) {
			slf4jLogger.error("Error retrieving articles for cwid=[" + cwid + "].", e);
		}
		
		// Save the articles.
		pubMedService.save(pubMedArticles);
		
		// Save the search result.
		List<Long> pmids = new ArrayList<Long>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
		}
		if (!pmids.isEmpty()) {
			eSearchResultService.save(new ESearchResult(cwid, pmids));
		}
		
		return pmids;
	}

	@RequestMapping(value = "/reciter/pubmedarticle/matchinglastname/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<PubMedArticle> getAuthorsWithMatchingLastName(@RequestParam(value="cwid") String cwid) {
		// Get target author information.
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		
		List<PubMedArticle> pubMedArticles = pubMedService.findMatchingAuthorsByLastName("test");
		
		return pubMedArticles;
	}
	
}