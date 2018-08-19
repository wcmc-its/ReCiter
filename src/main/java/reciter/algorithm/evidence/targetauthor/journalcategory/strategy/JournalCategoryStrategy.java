package reciter.algorithm.evidence.targetauthor.journalcategory.strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;
import reciter.model.pubmed.MedlineCitationJournalISSN;
import reciter.service.ScienceMetrixService;

public class JournalCategoryStrategy extends AbstractTargetAuthorStrategy {
	
	private static final Logger log = LoggerFactory.getLogger(JournalCategoryStrategy.class);
	private final List<String> orgUnitSynonym = Arrays.asList(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitSynonym().trim().split("\\s*,\\s*"));
	
	@Autowired
	private ScienceMetrixService scienceMetrixService;

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		Set<OrganizationalUnit> sanitizedIdentityInstitutions = new HashSet<OrganizationalUnit>();
		Map<String, List<String>> identityOrgUnitToSynonymMap = new HashMap<String, List<String>>();
		populateSanitizedIdentityInstitutions(identity, sanitizedIdentityInstitutions, identityOrgUnitToSynonymMap);
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			if(reCiterArticle.getJournal().getJournalIssn() != null 
					&&
					reCiterArticle.getJournal().getJournalIssn().size() > 0) {
				ScienceMetrix scienceMetrix = checkIssnInScienceMetrix(reCiterArticle.getJournal().getJournalIssn());
				if(scienceMetrix != null) {
					log.info(scienceMetrix.getPublicationName());
				}

			}
			if (reCiterArticle.getArticleCoAuthors() != null && reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
				for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
					if(author.isTargetAuthor() 
							&& 
							author.getAffiliation() != null) {
						
					}
				}
			}
		}
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
	
	private ScienceMetrix checkIssnInScienceMetrix(List<MedlineCitationJournalISSN> journalIssns) {
		String issnPrint = null;
		String issnElectronic = null;
		String issnLinking = null;
		ScienceMetrix scienceMetrix = null;
		for(MedlineCitationJournalISSN journalIssn: journalIssns) {
			if(journalIssn.getIssntype().equalsIgnoreCase("Print")) {
				issnPrint = journalIssn.getIssn().trim();
			} else if(journalIssn.getIssntype().equalsIgnoreCase("Electronic")) {
				issnElectronic = journalIssn.getIssn().trim();
			} else {
				issnLinking = journalIssn.getIssn().trim();
			}
		}
		try {
			if(issnLinking != null) {
				scienceMetrix = scienceMetrixService.findByIssn(issnLinking);
				if(scienceMetrix == null) {
					scienceMetrix = scienceMetrixService.findByEissn(issnLinking);
				}
			} else if(scienceMetrix == null && issnPrint != null) {
				scienceMetrix = scienceMetrixService.findByIssn(issnPrint);
				if(scienceMetrix == null) {
					scienceMetrix = scienceMetrixService.findByEissn(issnPrint);
				}
			} else if(scienceMetrix == null && issnElectronic != null) {
				scienceMetrix = scienceMetrixService.findByIssn(issnElectronic);
				if(scienceMetrix == null) {
					scienceMetrix = scienceMetrixService.findByEissn(issnElectronic);
				}
			}
		} catch(NullPointerException npe) {
			return scienceMetrix;
		}
		return scienceMetrix;
	}
	
	/**
	 * This function gets orgUnits from Identity and home organizationalUnitsSynonym if declared in application.properties and return a unique set of Departments.
	 * It also Substitute any and for & and vise versa in identity.departments Remove any commas or dashes from identity.departments. Remove any commas or dashes from article.affiliation.
	 * Substitute any Tri-I for Tri-Institutional and vise versa.
	 * @see <a href="https://github.com/wcmc-its/ReCiter/issues/264">Issue Details</a>
	 * @param identity
	 * @param identityOrgUnitToSynonymMap
	 */
	private void populateSanitizedIdentityInstitutions(Identity identity, Set<OrganizationalUnit> sanitizedIdentityInstitutions, Map<String, List<String>> identityOrgUnitToSynonymMap) {
		if(identity.getOrganizationalUnits() != null
				&&
				identity.getOrganizationalUnits().size() > 0) {
			for(OrganizationalUnit orgUnit: identity.getOrganizationalUnits()) {
				if(this.orgUnitSynonym.stream().anyMatch(syn -> StringUtils.containsIgnoreCase(syn, orgUnit.getOrganizationalUnitLabel()))) {
					List<String> matchedOrgUnitSynonnym = this.orgUnitSynonym.stream().filter(syn -> StringUtils.containsIgnoreCase(syn, orgUnit.getOrganizationalUnitLabel())).collect(Collectors.toList());
					for(String orgUnitsynonyms: matchedOrgUnitSynonnym) {
						List<String> synonyms = Arrays.asList(orgUnitsynonyms.trim().split("\\s*\\|\\s*"));
						Set<OrganizationalUnit> orgUnitWithSynonmyms = new HashSet<OrganizationalUnit>(synonyms.size());
						
						identityOrgUnitToSynonymMap.put(orgUnit.getOrganizationalUnitLabel(), synonyms);
						//synonyms.forEach(synonym -> {
						for(String synonym: synonyms) {
							OrganizationalUnit orgUnitWithSynonmym = new OrganizationalUnit();
							orgUnitWithSynonmym.setOrganizationalUnitLabel(synonym.trim());
							orgUnitWithSynonmym.setOrganizationalUnitType("department");
							orgUnitWithSynonmyms.add(orgUnitWithSynonmym);
						}
						if(orgUnitWithSynonmyms.size() > 0) {
							sanitizedIdentityInstitutions.addAll(orgUnitWithSynonmyms);
						}
					}
				} else {
					sanitizedIdentityInstitutions.add(orgUnit);
				}
			}
		}
	}

}
