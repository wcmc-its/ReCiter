package reciter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.model.author.TargetAuthor;
import reciter.service.BoardCertificationService;
import reciter.service.TargetAuthorService;
import reciter.xml.parser.pubmed.model.PubMedArticle;
import reciter.xml.retriever.engine.DefaultReCiterRetrievalEngine;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
@EnableAutoConfiguration
public class ReCiterController {

	@Autowired
	private BoardCertificationService boardCertificationService;
	
	@Autowired
	private TargetAuthorService targetAuthorService;
	
	@RequestMapping("/")
	@ResponseBody
	public List<String> home() {
		List<String> list = boardCertificationService.getBoardCertificationsByCwid("ccole");
		return list;
	}

	@RequestMapping(value = "/reciter/targetauthor/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public TargetAuthor getTargetAuthorByCwid(@RequestParam(value="cwid") String cwid) {
		ReCiterRetrievalEngine retrievalEngine = new DefaultReCiterRetrievalEngine();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		return targetAuthor;
	}
	
	@RequestMapping(value = "/reciter/pubmedarticle/by/pmid", method = RequestMethod.GET)
	@ResponseBody
	public PubMedArticle getPubMedArticleByPmid(@RequestParam(value="pmid") long pmid) {
		return null;
	}
}