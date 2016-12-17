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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.algorithm.util.ArticleTranslator;
import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.MeshTerm;
import reciter.database.mongo.model.PubMedArticleFeature;
import reciter.engine.Engine;
import reciter.engine.EngineParameters;
import reciter.engine.Feature;
import reciter.engine.ReCiterEngine;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.MeshTermService;
import reciter.service.PubMedArticleFeatureService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.service.TrainingDataService;
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
	private PubMedArticleFeatureService pubMedArticleFeatureService;
	

}