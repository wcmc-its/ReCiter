package reciter.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.Identity;
import reciter.database.mongo.model.MeshTerm;
import reciter.database.mongo.model.PubMedArticleFeature;
import reciter.engine.Engine;
import reciter.engine.Feature;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.MeshTermService;
import reciter.service.PubMedArticleFeatureService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.service.TrainingDataService;
import reciter.xml.parser.translator.ArticleTranslator;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
public class ReCiterController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);

	private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	@Autowired
	private ESearchResultService eSearchResultService;

	@Autowired
	private PubMedService pubMedService;
	
	@Autowired
	private ReCiterRetrievalEngine aliasReCiterRetrievalEngine;
	
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
	
	@RequestMapping(value = "/reciter/save", method = RequestMethod.GET)
	@ResponseBody
	public void saveIdentity() {
		Identity identity = new Identity();
		identity.setCwid("test");
		identityService.delete(identity);
	}
	
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

	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/retrieve/article/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public void retrieveArticles(@RequestParam(value="cwid") String cwid) {
		Identity identity = identityService.findByCwid(cwid);
		try {
			aliasReCiterRetrievalEngine.retrieve(identity);
		} catch (IOException e) {
			slf4jLogger.error("Unable to retrieve articles for cwid=[" + cwid + "]", e);
		}
	}
	
	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/retrieve/article/by/cwid/date", method = RequestMethod.GET)
	@ResponseBody
	public void retrieveArticlesByDateRange(@RequestParam(value="cwid") String cwid, 
			@RequestParam(value="startDate") String startDate,
			@RequestParam(value="endDate") String endDate) {
		
		LocalDate startLocalDate = null;
		LocalDate endLocalDate = null;
		try {
			startLocalDate = LocalDate.parse(startDate, DATE_FORMAT);
			endLocalDate = LocalDate.parse(endDate, DATE_FORMAT);
		} catch (DateTimeParseException e) {
			slf4jLogger.error("Error parsing dates. Please use date format yyyy-MM-dd", e);
			return;
		}
		
		Identity identity = identityService.findByCwid(cwid);
		try {
			aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identity, startLocalDate, endLocalDate);
		} catch (IOException e) {
			slf4jLogger.error("Unable to retrieve articles for cwid=[" + cwid + "]", e);
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
			pmids.addAll(eSearchResult.getESearchPmid().getPmids());
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
		analysis.setESearchResults(eSearchResults);
		analysis.setIdentity(identity);
		
		slf4jLogger.info(analysis.toString());
		return analysis;
	}
	
	@RequestMapping(value = "/reciter/feature/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<Feature> generateFeature(@RequestParam(value="cwid") String cwid) {
		
		Identity identiy = identityService.findByCwid(cwid);
		List<ESearchResult> eSearchResults = eSearchResultService.findByCwid(cwid);
		Set<Long> pmids = new HashSet<Long>();
		for (ESearchResult eSearchResult : eSearchResults) {
			pmids.addAll(eSearchResult.getESearchPmid().getPmids());
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
	
	@CrossOrigin(origins = "http://localhost:9000")
	@RequestMapping(value = "/reciter/download/log", method = RequestMethod.GET)
	@ResponseBody
	public void downloadLog(final HttpServletRequest request, final HttpServletResponse response) {

        File file = new File("logs/reciter.log");
        try (InputStream fileInputStream = new FileInputStream(file);
                OutputStream output = response.getOutputStream();) {

            response.reset();

            response.setContentType("application/octet-stream");
            response.setContentLength((int) (file.length()));

            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

            IOUtils.copyLarge(fileInputStream, output);
            output.flush();
        } catch (IOException e) {
            slf4jLogger.error(e.getMessage(), e);
        }
	}
}