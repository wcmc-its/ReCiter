package reciter.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.Identity;
import reciter.database.mongo.model.MeshTerm;
import reciter.database.mongo.model.PubMedArticleFeature;
import reciter.database.mongo.model.TrainingData;
import reciter.engine.Engine;
import reciter.engine.Feature;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.MeshTermService;
import reciter.service.PubMedArticleFeatureService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.service.TrainingDataService;
import reciter.xml.parser.scopus.model.ScopusArticle;
import reciter.xml.parser.translator.ArticleTranslator;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
public class ReCiterController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);

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
	
	@Autowired
	private ScopusService scopusService;
	
	@Autowired
	private MeshTermService meshTermService;
	
	@Autowired
	private TrainingDataService trainingDataService;
	
	@Autowired
	private PubMedArticleFeatureService pubMedArticleFeatureService;
	
	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/esearchresult/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<ESearchResult> index(@RequestParam(value="cwid") String cwid) {
		return eSearchResultService.findByCwid(cwid);
	}
	
	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/identitybeans/by/search", method = RequestMethod.GET)
	@ResponseBody
	public List<Identity> getTargetAuthorByNameOrCwid(@RequestParam(value="search") String search) {
		return identityService.findByCwidRegex(search);
	}

	@RequestMapping(value = "/reciter/retrieve/article/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<Long> retrieveArticles(@RequestParam(value="cwid") String cwid) {
		Identity identity = identityService.findByCwid(cwid);
		return defaultReCiterRetrievalEngine.retrieve(identity);
	}
	
	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/retrieve/limited/article/by/cwid", method = RequestMethod.GET)
	@Async
	public void retrieveLimitedArticles(@RequestParam(value="cwid") String cwid) {
		Identity identity = identityService.findByCwid(cwid);
		defaultReCiterRetrievalEngine.retrieveWithMultipleStrategies(identity);
	}
	
	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/retrieve/limited/article/for/testcwids", method = RequestMethod.GET)
	@Async
	public void retrieveLimitedArticlesForTestCwids() {
		List<TrainingData> trainingDatas = trainingDataService.findAll();
		if (!trainingDatas.isEmpty()) {
			List<String> cwids = trainingDatas.get(0).getCwids();
			for (String cwid : cwids) {
				Identity identity = identityService.findByCwid(cwid);
				defaultReCiterRetrievalEngine.retrieveWithMultipleStrategies(identity);
			}
		}
	}

	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/analysis/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public Analysis runAnalysis(@RequestParam(value="cwid") String cwid) {
		
		Identity identity = identityService.findByCwid(cwid);
		List<ESearchResult> eSearchResults = eSearchResultService.findByCwid(cwid);
		Set<Long> pmids = new HashSet<Long>();
		for (ESearchResult eSearchResult : eSearchResults) {
			pmids.addAll(eSearchResult.geteSearchPmid().getPmids());
		}
		List<Long> pmidList = new ArrayList<Long>(pmids);
		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(pmidList);
		List<ScopusArticle> scopusArticles = scopusService.findByPubmedId(pmidList);
		Map<Long, ScopusArticle> map = new HashMap<Long, ScopusArticle>();
		for (ScopusArticle scopusArticle : scopusArticles) {
			map.put(scopusArticle.getPubmedId(), scopusArticle);
		}
		List<ReCiterArticle> reCiterArticles = new ArrayList<ReCiterArticle>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			long pmid = pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid();
			if (map.containsKey(pmid)) {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, map.get(pmid)));
			} else {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null));
			}
		}
		
		if (reCiterEngine.getMeshTermCache() == null) {
			List<MeshTerm> meshTerms = meshTermService.findAll();
			Map<String, Long> meshTermCache = new HashMap<String, Long>();
			for (MeshTerm meshTerm : meshTerms) {
				meshTermCache.put(meshTerm.getMesh(), meshTerm.getCount());
			}
			reCiterEngine.setMeshTermCache(meshTermCache);
		}
		
		Analysis analysis = reCiterEngine.run(identity, reCiterArticles);
		slf4jLogger.info(analysis.toString());
		return analysis;
	}
	
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
		defaultReCiterRetrievalEngine.retrieve(identity);
		return "Success";
	}
	
	@RequestMapping(value = "/reciter/feature/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<Feature> generateFeature(@RequestParam(value="cwid") String cwid) {
		
		Identity identiy = identityService.findByCwid(cwid);
		List<ESearchResult> eSearchResults = eSearchResultService.findByCwid(cwid);
		Set<Long> pmids = new HashSet<Long>();
		for (ESearchResult eSearchResult : eSearchResults) {
			pmids.addAll(eSearchResult.geteSearchPmid().getPmids());
		}
		List<Long> pmidList = new ArrayList<Long>(pmids);
		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(pmidList);
		List<ScopusArticle> scopusArticles = scopusService.findByPubmedId(pmidList);
		Map<Long, ScopusArticle> map = new HashMap<Long, ScopusArticle>();
		for (ScopusArticle scopusArticle : scopusArticles) {
			map.put(scopusArticle.getPubmedId(), scopusArticle);
		}
		List<ReCiterArticle> reCiterArticles = new ArrayList<ReCiterArticle>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			long pmid = pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid();
			if (map.containsKey(pmid)) {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, map.get(pmid)));
			} else {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null));
			}
		}
		List<Feature> features = reCiterEngine.generateFeature(identiy, reCiterArticles);
		PubMedArticleFeature pubMedArticleFeature = new PubMedArticleFeature();
		pubMedArticleFeature.setCwid(cwid);
		pubMedArticleFeature.setFeatures(features);
		pubMedArticleFeatureService.save(pubMedArticleFeature);
		return features;
	}
}