package reciter.controller;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
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
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
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
	
	@RequestMapping(value="/",method = RequestMethod.GET)
    public String homepage(){
        return "index";
    }
	
	@RequestMapping(value = "/reciter/esearchresult/by/cwid", method = RequestMethod.GET)
	@ResponseBody
    public ESearchResult index(@RequestParam(value="cwid") String cwid) {
		return eSearchResultService.findByCwid(cwid);
    }

	@RequestMapping(value = "/reciter/targetauthor/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public TargetAuthor getTargetAuthorByCwid(@RequestParam(value="cwid") String cwid) {
		return targetAuthorService.getTargetAuthor(cwid);
	}
	
	@RequestMapping(value = "/reciter/test", method = RequestMethod.GET)
	@ResponseBody
	public List<PubMedArticle> findByMedlineCitationMedlineCitationPMIDPmid() {
		return null;
	}
	
	@RequestMapping(value = "/reciter/authornames/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public Set<AuthorName> findUniqueAuthorsWithSameLastNameAsTargetAuthor(@RequestParam(value="cwid") String cwid) {
		// Get target author information.
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		return defaultReCiterRetrievalEngine.findUniqueAuthorsWithSameLastNameAsTargetAuthor(targetAuthor);
	}
	
	@RequestMapping(value = "/reciter/pubmedarticle/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public void retrievePubMedArticles(@RequestParam(value="cwid") String cwid) {
		// Get target author information.
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		defaultReCiterRetrievalEngine.retrieve(targetAuthor);
	}
}