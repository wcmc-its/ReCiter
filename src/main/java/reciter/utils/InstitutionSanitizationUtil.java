package reciter.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.engine.StrategyParameters;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;
import reciter.model.identity.OrganizationalUnit.OrganizationalUnitType;

@NoArgsConstructor
public class InstitutionSanitizationUtil {
	
	private StrategyParameters strategyParameters;
	
	private List<String> orgUnitSynonym;
	
	public InstitutionSanitizationUtil(StrategyParameters strategyParameters) {
		this.strategyParameters = strategyParameters;
		this.orgUnitSynonym = Arrays.asList(this.strategyParameters.getOrganizationalUnitSynonym().trim().split("\\s*,\\s*"));
	}
	
	/**
	 * This function gets orgUnits from Identity and home organizationalUnitsSynonym if declared in application.properties and return a unique set of Departments.
	 * It also Substitute any and for & and vise versa in identity.departments Remove any commas or dashes from identity.departments. Remove any commas or dashes from article.affiliation.
	 * Substitute any Tri-I for Tri-Institutional and vise versa.
	 * @see <a href="https://github.com/wcmc-its/ReCiter/issues/264">Issue Details</a>
	 * @param identity
	 * @param identityOrgUnitToSynonymMap
	 */
	public void populateSanitizedIdentityInstitutions(Identity identity) {
		Set<OrganizationalUnit> sanitizedIdentityInstitutions = new HashSet<OrganizationalUnit>();
		Map<String, List<String>> identityOrgUnitToSynonymMap = new HashMap<String, List<String>>();
		
		List<List<String>> orgUnitSynonym = new ArrayList<List<String>>();
		if(identity.getOrganizationalUnits() != null
				&&
				identity.getOrganizationalUnits().size() > 0) {
			for(String orgUnits: this.orgUnitSynonym) {
				List<String> synonyms =  Arrays.asList(orgUnits.trim().split("\\s*\\|\\s*"));
				
				orgUnitSynonym.add(synonyms);
			}
			for(OrganizationalUnit orgUnit: identity.getOrganizationalUnits()) {
				//if(this.orgUnitSynonym.stream().anyMatch(syn -> StringUtils.containsIgnoreCase(syn, orgUnit.getOrganizationalUnitLabel()))) {
				if(orgUnitSynonym.stream().anyMatch(syn -> syn.contains(orgUnit.getOrganizationalUnitLabel()))) {
					List<List<String>> matchedOrgUnitSynonnym = orgUnitSynonym.stream().filter(syn -> syn.contains(orgUnit.getOrganizationalUnitLabel())).collect(Collectors.toList());
					for(List<String> orgUnitsynonyms: matchedOrgUnitSynonnym) {
						Set<OrganizationalUnit> orgUnitWithSynonmyms = new HashSet<OrganizationalUnit>(orgUnitsynonyms.size());
						identityOrgUnitToSynonymMap.put(orgUnit.getOrganizationalUnitLabel(), orgUnitsynonyms);
						for(String synonym: orgUnitsynonyms) {
							OrganizationalUnit orgUnitWithSynonmym = new OrganizationalUnit();
							orgUnitWithSynonmym.setOrganizationalUnitLabel(synonym.trim());
							orgUnitWithSynonmym.setOrganizationalUnitType(OrganizationalUnitType.DEPARTMENT);
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
		identity.setSanitizedIdentityInstitutions(sanitizedIdentityInstitutions);
		identity.setIdentityOrgUnitToSynonymMap(identityOrgUnitToSynonymMap);
	}


}
