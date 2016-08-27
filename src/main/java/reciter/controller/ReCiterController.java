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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.database.dao.impl.MeshRawCountImpl;
import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.Identity;
import reciter.database.mongo.model.MeshTerm;
import reciter.engine.Engine;
import reciter.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.MeshTermService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.service.TargetAuthorService;
import reciter.xml.parser.scopus.model.ScopusArticle;
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
	
	@Autowired
	private ScopusService scopusService;
	
	@Autowired
	private MeshTermService meshTermService;
	
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
	public List<Identity> getTargetAuthorByNameOrCwid(@RequestParam(value="search") String search) {
		return identityService.findByCwidRegex(search);
	}

	@RequestMapping(value = "/reciter/test", method = RequestMethod.GET)
	@ResponseBody
	public List<PubMedArticle> findByMedlineCitationMedlineCitationPMIDPmid() {
		return null;
	}
	
	@RequestMapping(value = "/reciter/migrate/goldstandard", method = RequestMethod.GET)
	@ResponseBody
	public void migrateGoldStandard() {
//		IdentityDaoImpl identityDaoImpl = new IdentityDaoImpl();
//		List<reciter.database.model.Identity> identities = identityDaoImpl.getAllIdentities();
//		List<reciter.database.mongo.model.Identity> mongoIdentities = new ArrayList<reciter.database.mongo.model.Identity>();
//		GoldStandardPmidsDaoImpl g = new GoldStandardPmidsDaoImpl();
//		Map<String, List<Long>> pmids = g.getGoldStandard();
////		for (Entry<String, List<Long>> entry : pmids.entrySet()) {
////			GoldStandard goldStandard = new GoldStandard();
////			goldStandard.setCwid(entry.getKey());
////			goldStandard.setPmids(entry.getValue());
////			goldStandardService.save(goldStandard);
////		}
//		Map<String, IdentityDegree> degrees = new IdentityDegreeDaoImpl().getAllIdentityDegree();
//		Map<String, List<reciter.database.mongo.model.Grant>> grants = new IdentityGrantDaoImpl().getAllIdentityGrant();
//		for (reciter.database.model.Identity identity : identities) {
//			reciter.database.mongo.model.Identity i = new reciter.database.mongo.model.Identity();
//			i.setCwid(identity.getCwid());
//			i.setAuthorName(new AuthorName(identity.getFirstName(), identity.getMiddleName(), identity.getLastName()));
//			List<String> emails = new ArrayList<String>();
//			if (identity.getEmail() != null && !identity.getEmail().isEmpty()) {
//				emails.add(identity.getEmail());
//			}
//			if (identity.getEmailOther() != null && !identity.getEmailOther().isEmpty()) {
//				emails.add(identity.getEmailOther());
//			}
//			i.setEmails(emails);
//			List<String> departments = new ArrayList<String>();
//			if (identity.getPrimaryDepartment() != null && !identity.getPrimaryDepartment().isEmpty()) {
//				departments.add(identity.getPrimaryDepartment());
//			}
//			if (identity.getOtherDepartment() != null && !identity.getOtherDepartment().isEmpty()) {
//				departments.add(identity.getOtherDepartment());
//			}
//			i.setDepartments(departments);
//			if (identity.getTitle() != null && !identity.getTitle().isEmpty()) {
//				i.setTitle(identity.getTitle());
//			}
//			List<String> affiliations = new ArrayList<String>();
//			if (identity.getPrimaryAffiliation() != null && !identity.getPrimaryAffiliation().isEmpty()) {
//				affiliations.add(identity.getPrimaryAffiliation());
//			}
//			i.setAffiliations(affiliations);
//			
//			if (pmids.containsKey(identity.getCwid())) {
//				i.setKnownPmids(pmids.get(identity.getCwid()));
//			}
//			
//			if (grants.containsKey(identity.getCwid())) {
//				i.setGrants(grants.get(identity.getCwid()));
//			}
//			
//			if (degrees.containsKey(identity.getCwid())) {
//				IdentityDegree degree = degrees.get(identity.getCwid());
//				Education b = new Education();
//				b.setDegreeYear(degree.getBachelor());
//				i.setBachelor(b);
//				
//				Education m = new Education();
//				m.setDegreeYear(degree.getMasters());
//				i.setMasters(m);
//				
//				Education d = new Education();
//				d.setDegreeYear(degree.getDoctoral());
//				i.setDoctoral(d);
//			}
//			
//			mongoIdentities.add(i);
//		}
//		identityService.save(mongoIdentities);
		List<MeshTerm> meshTerms = new MeshRawCountImpl().getAllMeshTerms();
		meshTermService.save(meshTerms);
	}

//	@RequestMapping(value = "/reciter/authornames/by/cwid", method = RequestMethod.GET)
//	@ResponseBody
//	public Set<AuthorName> findUniqueAuthorsWithSameLastNameAsTargetAuthor(@RequestParam(value="cwid") String cwid) {
//		// Get target author information.
//		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
//		return defaultReCiterRetrievalEngine.findUniqueAuthorsWithSameLastNameAsTargetAuthor(targetAuthor);
//	}

	@RequestMapping(value = "/reciter/retrieve/article/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public List<Long> retrieveArticles(@RequestParam(value="cwid") String cwid) {
		Identity identity = identityService.findByCwid(cwid);
		return defaultReCiterRetrievalEngine.retrieve(identity);
	}

//	@RequestMapping(value = "/reciter/pubmedarticle/by/cwids", method = RequestMethod.GET)
//	@ResponseBody
//	public void retrievePubMedArticlesForListOfCwid(@RequestParam(value="cwids") List<String> cwids) {
//		for (String cwid : cwids) {
//			TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
//			defaultReCiterRetrievalEngine.retrieve(targetAuthor);
//		}
//	}
	
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
		
		if (reCiterEngine.getMeshTermCache() == null) {
			List<MeshTerm> meshTerms = meshTermService.findAll();
			Map<String, Long> meshTermCache = new HashMap<String, Long>();
			for (MeshTerm meshTerm : meshTerms) {
				meshTermCache.put(meshTerm.getMesh(), meshTerm.getCount());
			}
			reCiterEngine.setMeshTermCache(meshTermCache);
		}
		
		Analysis analysis = reCiterEngine.run(identiy, reCiterArticles);
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
		defaultReCiterRetrievalEngine.retrieve(identity);
		return "Success";
	}
}