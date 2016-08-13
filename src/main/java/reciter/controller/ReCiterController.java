package reciter.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.database.mongo.model.ESearchPmid;
import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.Identity;
import reciter.engine.Engine;
import reciter.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.TargetAuthorService;
import reciter.service.bean.IdentityBean;
import reciter.xml.parser.translator.ArticleTranslator;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
public class ReCiterController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);

	@Autowired
	private TargetAuthorService targetAuthorService;

	@Autowired
	private ESearchResultService eSearchResultService;

	@Autowired
	private PubMedService pubMedService;
	
	@Autowired
	private ReCiterRetrievalEngine defaultReCiterRetrievalEngine;
	
	@Autowired
	private Engine reCiterEngine;
	
	@Autowired
	private IdentityService identityService;
	
	@RequestMapping(value="/",method = RequestMethod.GET)
	public String homepage(){
		return "index";
	}

	@RequestMapping(value = "/reciter/esearchresult/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<ESearchResult> index(@RequestParam(value="cwid") String cwid) {
		return eSearchResultService.findByCwid(cwid);
	}

	@RequestMapping(value = "/reciter/targetauthor/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public TargetAuthor getTargetAuthorByCwid(@RequestParam(value="cwid") String cwid) {
		return targetAuthorService.getTargetAuthor(cwid);
	}
	
	@RequestMapping(value = "/reciter/identitybeans/by/search", method = RequestMethod.GET)
	@ResponseBody
	public List<IdentityBean> getTargetAuthorByNameOrCwid(@RequestParam(value="search") String search) {
		return targetAuthorService.getTargetAuthorByNameOrCwid(search);
	}

	@RequestMapping(value = "/reciter/test", method = RequestMethod.GET)
	@ResponseBody
	public List<PubMedArticle> findByMedlineCitationMedlineCitationPMIDPmid() {
		return null;
	}

//	@RequestMapping(value = "/reciter/authornames/by/cwid", method = RequestMethod.GET)
//	@ResponseBody
//	public Set<AuthorName> findUniqueAuthorsWithSameLastNameAsTargetAuthor(@RequestParam(value="cwid") String cwid) {
//		// Get target author information.
//		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
//		return defaultReCiterRetrievalEngine.findUniqueAuthorsWithSameLastNameAsTargetAuthor(targetAuthor);
//	}

	@RequestMapping(value = "/reciter/pubmedarticle/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<Long> retrievePubMedArticles(@RequestParam(value="cwid") String cwid) {
		// Get target author information.
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		return defaultReCiterRetrievalEngine.retrieve(targetAuthor);
	}

	@RequestMapping(value = "/reciter/pubmedarticle/by/cwids", method = RequestMethod.GET)
	@ResponseBody
	public void retrievePubMedArticlesForListOfCwid(@RequestParam(value="cwids") List<String> cwids) {
		for (String cwid : cwids) {
			TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
			defaultReCiterRetrievalEngine.retrieve(targetAuthor);
		}
	}
	
	@RequestMapping(value = "/reciter/analysis/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public Analysis runAnalysis(@RequestParam(value="cwid") String cwid) {
//		System.out.println("test");
//		
//		Analysis analysis = new Analysis();
//		analysis.setTruePos(9);
//		analysis.setGoldStandardSize(10);
//		analysis.setSelectedClusterSize(9);
//		
//		List<AnalysisObject> analysisObjectList = new ArrayList<AnalysisObject>();
//		AnalysisObject a1 = new AnalysisObject();
//		a1.setStatus(StatusEnum.FALSE_NEGATIVE);
//		a1.setCwid("meb7002");
//		a1.setTargetName("Michael E. Bales");
//		a1.setPmid(26769910);
//		analysisObjectList.add(a1);
//		analysis.setAnalysisObjectList(analysisObjectList);
		
//		return analysis;
		
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		List<ESearchResult> eSearchResults = eSearchResultService.findByCwid(cwid);
		Set<Long> pmids = new HashSet<Long>();
		for (ESearchResult eSearchResult : eSearchResults) {
			pmids.addAll(eSearchResult.geteSearchPmid().getPmids());
		}
		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(new ArrayList<Long>(pmids));
		List<ReCiterArticle> reCiterArticles = new ArrayList<ReCiterArticle>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null));
		}
		Analysis analysis = reCiterEngine.run(targetAuthor, reCiterArticles);
		slf4jLogger.info(analysis.toString());
		return analysis;
	}
	
//	@RequestMapping(value = "/reciter/data_import/rc_identity", method = RequestMethod.POST)
//	@ResponseBody
//	public String dataImport(@RequestBody Identity identity) {
//		System.out.println(identity);
//		return "Success";
//	}
	
	@RequestMapping(value = "/reciter/data_import/rc_identity", method = RequestMethod.POST)
	@ResponseBody
	public String importIdentity(@RequestBody List<Identity> identities) {
		System.out.println(identities);
//		identityService.save(identities);
		return "Success";
	}
	
	@RequestMapping(value = "/reciter/newidentity/", method = RequestMethod.POST)
	@ResponseBody
	public String importNewIdentity(@RequestBody Identity identity) {
		identityService.save(identity);
		TargetAuthor targetAuthor = targetAuthorService.convertToTargetAuthor(identity);
		defaultReCiterRetrievalEngine.retrieve(targetAuthor);
		return "Success";
	}
}