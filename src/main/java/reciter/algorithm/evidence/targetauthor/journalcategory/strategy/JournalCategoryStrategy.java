package reciter.algorithm.evidence.targetauthor.journalcategory.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

import reciter.ApplicationContextHolder;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.controller.ReCiterController;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.JournalCategoryEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;
import reciter.model.pubmed.MedlineCitationJournalISSN;
import reciter.service.ScienceMetrixDepartmentCategoryService;
import reciter.service.ScienceMetrixService;

public class JournalCategoryStrategy extends AbstractTargetAuthorStrategy {
	
	private static final Logger log = LoggerFactory.getLogger(JournalCategoryStrategy.class);
	private final List<String> orgUnitSynonym = Arrays.asList(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitSynonym().trim().split("\\s*,\\s*"));
	
	private ScienceMetrixService scienceMetrixService = ApplicationContextHolder.getContext().getBean(ScienceMetrixService.class);
	
	private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService = ApplicationContextHolder.getContext().getBean(ScienceMetrixDepartmentCategoryService.class);

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		Set<String> sanitizedIdentityInstitutions = new HashSet<String>();
		Map<String, List<String>> identityOrgUnitToSynonymMap = new HashMap<String, List<String>>();
		populateSanitizedIdentityInstitutions(identity, sanitizedIdentityInstitutions, identityOrgUnitToSynonymMap);
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			if(reCiterArticle.getJournal().getJournalIssn() != null 
					&&
					reCiterArticle.getJournal().getJournalIssn().size() > 0) {
				JournalCategoryEvidence journalCategoryEvidence = null;
				ScienceMetrix scienceMetrix = checkIssnInScienceMetrix(reCiterArticle.getJournal().getJournalIssn());
				if(scienceMetrix != null) {
					List<ScienceMetrixDepartmentCategory> scienceMetrixDeptCategories = getScienceMetrixDepartmentCategory(scienceMetrix.getScienceMatrixSubfieldId());
					List<ScienceMetrixDepartmentCategory> matchedOrgUnits = scienceMetrixDeptCategories.stream().filter(sciMetrixDeptCategory -> 
					sanitizedIdentityInstitutions.contains(sciMetrixDeptCategory.getPrimaryDepartment().trim())
					).collect(Collectors.toList());
					if(matchedOrgUnits.size() > 0) {
						if(matchedOrgUnits.size() > 1) {
							ScienceMetrixDepartmentCategory matchedJournal = matchedOrgUnits.stream().max(Comparator.comparing(ScienceMetrixDepartmentCategory::getLogOddsRatio)).orElse(null);
							if(matchedJournal != null) {
								journalCategoryEvidence = new JournalCategoryEvidence();
								journalCategoryEvidence.setJournalSubfieldScienceMetrixLabel(matchedJournal.getScienceMetrixJournalSubfield());
								journalCategoryEvidence.setJournalSubfieldDepartment(matchedJournal.getPrimaryDepartment());
								journalCategoryEvidence.setJournalSubfieldScienceMetrixID(matchedJournal.getScienceMetrixJournalSubfieldId());
								journalCategoryEvidence.setJournalSubfieldScore(ReCiterArticleScorer.strategyParameters.getJournalSubfieldFactorScore() * matchedJournal.getLogOddsRatio());
							}
						} else {
							journalCategoryEvidence = new JournalCategoryEvidence();
							journalCategoryEvidence.setJournalSubfieldScienceMetrixLabel(matchedOrgUnits.get(0).getScienceMetrixJournalSubfield());
							journalCategoryEvidence.setJournalSubfieldDepartment(matchedOrgUnits.get(0).getPrimaryDepartment());
							journalCategoryEvidence.setJournalSubfieldScienceMetrixID(matchedOrgUnits.get(0).getScienceMetrixJournalSubfieldId());
							journalCategoryEvidence.setJournalSubfieldScore(ReCiterArticleScorer.strategyParameters.getJournalSubfieldFactorScore() * matchedOrgUnits.get(0).getLogOddsRatio());
						}
					} else {
						journalCategoryEvidence = new JournalCategoryEvidence();
						journalCategoryEvidence.setJournalSubfieldScienceMetrixLabel(scienceMetrix.getScienceMetrixSubfield());
						journalCategoryEvidence.setJournalSubfieldDepartment(scienceMetrix.getScienceMetrixField());
						journalCategoryEvidence.setJournalSubfieldScienceMetrixID(Integer.parseInt(scienceMetrix.getScienceMatrixSubfieldId()));
						journalCategoryEvidence.setJournalSubfieldScore(ReCiterArticleScorer.strategyParameters.getJournalSubfieldScore());
					}
				}
				if(journalCategoryEvidence != null) {
					log.info("Pmid: " + reCiterArticle.getArticleId() + " " + journalCategoryEvidence.toString());
				}
				reCiterArticle.setJournalCategoryEvidence(journalCategoryEvidence);
			}

		}
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
	
	private List<ScienceMetrixDepartmentCategory> getScienceMetrixDepartmentCategory(String subfieldId) {
		List<ScienceMetrixDepartmentCategory> scienceMetrixDeptCategory = null;
		if(subfieldId != null 
				&& 
				!subfieldId.isEmpty()) {
			scienceMetrixDeptCategory = scienceMetrixDepartmentCategoryService.findByScienceMetrixJournalSubfieldId(Long.parseLong(subfieldId));
		}
		return scienceMetrixDeptCategory;
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
	private void populateSanitizedIdentityInstitutions(Identity identity, Set<String> sanitizedIdentityInstitutions, Map<String, List<String>> identityOrgUnitToSynonymMap) {
		List<List<String>> orgUnitSynonym = new ArrayList<List<String>>();
		if(identity.getOrganizationalUnits() != null
				&&
				identity.getOrganizationalUnits().size() > 0) {
			for(String orgUnits: this.orgUnitSynonym) {
				List<String> synonyms =  Arrays.asList(orgUnits.trim().split("\\s*\\|\\s*"));
				
				orgUnitSynonym.add(synonyms);
			}
			for(OrganizationalUnit orgUnit: identity.getOrganizationalUnits()) {
				if(orgUnitSynonym.stream().anyMatch(syn -> syn.contains(orgUnit.getOrganizationalUnitLabel()))) {
					List<List<String>> matchedOrgUnitSynonym = orgUnitSynonym.stream().filter(syn -> syn.contains(orgUnit.getOrganizationalUnitLabel())).collect(Collectors.toList());
					for(List<String> orgUnitsynonyms: matchedOrgUnitSynonym) {
						
						identityOrgUnitToSynonymMap.put(orgUnit.getOrganizationalUnitLabel(), orgUnitsynonyms);
						if(orgUnitsynonyms.size() > 0) {
							sanitizedIdentityInstitutions.addAll(orgUnitsynonyms);
						}
					}
				} else {
					sanitizedIdentityInstitutions.add(orgUnit.getOrganizationalUnitLabel());
				}
			}
		}
	}

}
