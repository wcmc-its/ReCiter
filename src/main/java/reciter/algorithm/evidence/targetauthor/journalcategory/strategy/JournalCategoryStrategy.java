package reciter.algorithm.evidence.targetauthor.journalcategory.strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.engine.EngineParameters;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.JournalCategoryEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterJournalCategory;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;
import reciter.model.identity.OrganizationalUnit.OrganizationalUnitType;
import reciter.model.pubmed.MedlineCitationJournalISSN;

public class JournalCategoryStrategy extends AbstractTargetAuthorStrategy {
	
	private static final Logger log = LoggerFactory.getLogger(JournalCategoryStrategy.class);

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		Set<OrganizationalUnit> sanitizedIdentityInstitutions = identity.getSanitizedIdentityInstitutions();
		Map<String, List<String>> identityOrgUnitToSynonymMap = identity.getIdentityOrgUnitToSynonymMap();
	
		//	for (ReCiterArticle reCiterArticle : reCiterArticles) {
		
		reCiterArticles.forEach(reCiterArticle -> {
			if(reCiterArticle.getJournal().getJournalIssn() != null 
					&&
					reCiterArticle.getJournal().getJournalIssn().size() > 0) {
				JournalCategoryEvidence journalCategoryEvidence = null;
				ScienceMetrix scienceMetrix = checkIssnInScienceMetrix(reCiterArticle.getJournal().getJournalIssn());
				if(scienceMetrix != null) {
					List<ScienceMetrixDepartmentCategory> scienceMetrixDeptCategories = getScienceMetrixDepartmentCategory(scienceMetrix.getScienceMatrixSubfieldId());
					List<ScienceMetrixDepartmentCategory> matchedOrgUnits = scienceMetrixDeptCategories.stream().filter(sciMetrixDeptCategory -> 
					sanitizedIdentityInstitutions.stream().map(OrganizationalUnit::getOrganizationalUnitLabel).anyMatch(sciMetrixDeptCategory.getPrimaryDepartment().trim()::equalsIgnoreCase))
							.collect(Collectors.toList());
					
					if(scienceMetrixDeptCategories != null && !scienceMetrixDeptCategories.isEmpty()) {
						ReCiterJournalCategory reCiterJournalCategory = new ReCiterJournalCategory();
						reCiterJournalCategory.setJournalCategoryID(scienceMetrixDeptCategories.get(0).getScienceMetrixJournalSubfieldId());
						reCiterJournalCategory.setJournalCategoryLabel(scienceMetrixDeptCategories.get(0).getScienceMetrixJournalSubfield());
						reCiterArticle.setJournalCategory(reCiterJournalCategory);
					}
					if(matchedOrgUnits.size() > 0) {
						if(matchedOrgUnits.size() > 1) {
							ScienceMetrixDepartmentCategory matchedJournal = matchedOrgUnits.stream().max(Comparator.comparing(ScienceMetrixDepartmentCategory::getLogOddsRatio)).orElse(null);
							if(matchedJournal != null) {
								journalCategoryEvidence = new JournalCategoryEvidence();
								OrganizationalUnit journalSubFieldDepartment = sanitizedIdentityInstitutions.stream()
								.filter(sanitizedInst -> matchedJournal.getPrimaryDepartment().equalsIgnoreCase(sanitizedInst.getOrganizationalUnitLabel()))
								.findFirst()
								.orElse(new OrganizationalUnit(matchedJournal.getPrimaryDepartment(), OrganizationalUnitType.DEPARTMENT));
								String synonymOrgUnitLabel = null;
								//Finding the synonym
								if(identityOrgUnitToSynonymMap != null && identityOrgUnitToSynonymMap.size() > 0) {
									 synonymOrgUnitLabel = identityOrgUnitToSynonymMap.entrySet().stream().
											filter(synonymOrgUnit -> synonymOrgUnit.getValue().contains(journalSubFieldDepartment.getOrganizationalUnitLabel()))
											.map(Map.Entry::getKey)
											.findFirst()
											.orElse(null);
								}
								
								journalCategoryEvidence.setJournalSubfieldScienceMetrixLabel(matchedJournal.getScienceMetrixJournalSubfield());
								if(journalSubFieldDepartment.getOrganizationalUnitLabel() != null) {
									if(synonymOrgUnitLabel != null) {
										journalCategoryEvidence.setJournalSubfieldDepartment(synonymOrgUnitLabel);
									} else {
										journalCategoryEvidence.setJournalSubfieldDepartment(journalSubFieldDepartment.getOrganizationalUnitLabel());
									}
								}
								journalCategoryEvidence.setJournalSubfieldScienceMetrixID(matchedJournal.getScienceMetrixJournalSubfieldId());
								journalCategoryEvidence.setJournalSubfieldScore(ReCiterArticleScorer.strategyParameters.getJournalSubfieldFactorScore() * matchedJournal.getLogOddsRatio());
							}
						} else {
							journalCategoryEvidence = new JournalCategoryEvidence();
							OrganizationalUnit journalSubFieldDepartment = sanitizedIdentityInstitutions.stream()
									.filter(sanitizedInst -> matchedOrgUnits.get(0).getPrimaryDepartment().equalsIgnoreCase(sanitizedInst.getOrganizationalUnitLabel()))
									.findFirst()
									.orElse(new OrganizationalUnit(matchedOrgUnits.get(0).getPrimaryDepartment(), OrganizationalUnitType.DEPARTMENT));
							
							String synonymOrgUnitLabel = null;
							//Finding the synonym
							if(identityOrgUnitToSynonymMap != null && identityOrgUnitToSynonymMap.size() > 0) {
								synonymOrgUnitLabel = identityOrgUnitToSynonymMap.entrySet().stream().
									filter(synonymOrgUnit -> synonymOrgUnit.getValue().contains(journalSubFieldDepartment.getOrganizationalUnitLabel()))
									.map(Map.Entry::getKey)
									.findFirst()
									.orElse(null);
							}
							
							journalCategoryEvidence.setJournalSubfieldScienceMetrixLabel(matchedOrgUnits.get(0).getScienceMetrixJournalSubfield());
							if(journalSubFieldDepartment.getOrganizationalUnitLabel() != null) {
								if(synonymOrgUnitLabel != null) {
									journalCategoryEvidence.setJournalSubfieldDepartment(synonymOrgUnitLabel);
								} else {
									journalCategoryEvidence.setJournalSubfieldDepartment(journalSubFieldDepartment.getOrganizationalUnitLabel());
								}
							}
							journalCategoryEvidence.setJournalSubfieldScienceMetrixID(matchedOrgUnits.get(0).getScienceMetrixJournalSubfieldId());
							journalCategoryEvidence.setJournalSubfieldScore(ReCiterArticleScorer.strategyParameters.getJournalSubfieldFactorScore() * matchedOrgUnits.get(0).getLogOddsRatio());
						}
					} else {
						journalCategoryEvidence = new JournalCategoryEvidence();
						journalCategoryEvidence.setJournalSubfieldScienceMetrixLabel(scienceMetrix.getScienceMetrixSubfield());
						journalCategoryEvidence.setJournalSubfieldDepartment("NO_MATCH");
						journalCategoryEvidence.setJournalSubfieldScienceMetrixID(Integer.parseInt(scienceMetrix.getScienceMatrixSubfieldId()));
						journalCategoryEvidence.setJournalSubfieldScore(ReCiterArticleScorer.strategyParameters.getJournalSubfieldScore());
					}
				}
				if(journalCategoryEvidence != null) {
					log.info("Pmid: " + reCiterArticle.getArticleId() + " " + journalCategoryEvidence.toString());
				}
				reCiterArticle.setJournalCategoryEvidence(journalCategoryEvidence);
			}

	//	}
	});
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
			scienceMetrixDeptCategory = EngineParameters.getScienceMetrixDepartmentCategories().parallelStream().filter(scienceMetrixDepartmentCategory -> 
			scienceMetrixDepartmentCategory.getScienceMetrixJournalSubfieldId() == Integer.parseInt(subfieldId)
					).collect(Collectors.toList());
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
		
		/*if(issnLinking != null) {
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
		}*/
		
		for(ScienceMetrix scienceMetrixJournal: EngineParameters.getScienceMetrixJournals()) {
			if(issnLinking != null) {
				if(scienceMetrixJournal.getIssn() != null 
						&&
						scienceMetrixJournal.getIssn().equals(issnLinking)) {
					scienceMetrix = scienceMetrixJournal;
					break;
				}
				if(scienceMetrix == null) {
					if(scienceMetrixJournal.getEissn() != null 
							&&
							scienceMetrixJournal.getEissn().equals(issnLinking)) {
						scienceMetrix = scienceMetrixJournal;
						break;
					}
				}
			} else if(scienceMetrix == null && issnPrint != null) {
				if(scienceMetrixJournal.getIssn() != null 
						&&
						scienceMetrixJournal.getIssn().equals(issnPrint)) {
					scienceMetrix = scienceMetrixJournal;
					break;
				}
				if(scienceMetrix == null) {
					if(scienceMetrixJournal.getEissn() != null 
							&&
							scienceMetrixJournal.getEissn().equals(issnPrint)) {
						scienceMetrix = scienceMetrixJournal;
						break;
					}
				}
			} else if(scienceMetrix == null && issnElectronic != null) {
				if(scienceMetrixJournal.getIssn() != null 
						&&
						scienceMetrixJournal.getIssn().equals(issnElectronic)) {
					scienceMetrix = scienceMetrixJournal;
					break;
				}
				if(scienceMetrix == null) {
					if(scienceMetrixJournal.getEissn() != null 
							&&
							scienceMetrixJournal.getEissn().equals(issnElectronic)) {
						scienceMetrix = scienceMetrixJournal;
						break;
					}
				}
			}
		}
		

		return scienceMetrix;
	}
}
