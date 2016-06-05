package reciter.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.BoardCertificationDao;
import reciter.database.dao.IdentityDao;
import reciter.database.dao.impl.BoardCertificationDaoImpl;
import reciter.database.dao.impl.IdentityDaoImpl;
import reciter.database.model.IdentityDegree;
import reciter.database.model.IdentityDirectory;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorDegree;
import reciter.model.author.AuthorEducation;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import reciter.service.IdentityAlternateDeptNamesService;
import reciter.service.IdentityCitizenshipService;
import reciter.service.IdentityDegreeService;
import reciter.service.IdentityDirectoryService;
import reciter.service.IdentityEducationService;
import reciter.service.IdentityEmailService;
import reciter.service.IdentityGrantService;
import reciter.service.IdentityInstitutionService;
import reciter.service.IdentityService;
import reciter.service.TargetAuthorService;
import reciter.service.bean.IdentityBean;
import reciter.service.converters.IdentityDegreeConverter;
import reciter.string.PubmedSearchQueryGenerator;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;

@Service("targetAuthorService")
public class TargetAuthorServiceImpl implements TargetAuthorService {

	@Autowired
	private IdentityService identityService;
	
	@Autowired
	private IdentityCitizenshipService identityCitizenshipService;
	
	@Autowired
	private IdentityDegreeService identityDegreeService;
	
	@Autowired
	private IdentityEducationService identityEducationService;
	
	@Autowired
	private IdentityInstitutionService identityInstitutionService;
	
	@Autowired
	private IdentityDirectoryService identityDirectoryService;
	
	@Autowired
	private IdentityAlternateDeptNamesService identityAlternateDeptNamesService;
	
	@Autowired
	private IdentityGrantService identityGrantService;
	
	@Autowired
	private IdentityEmailService identityEmailService;
	
	@Override
	public TargetAuthor getTargetAuthor(String cwid) {
		IdentityBean identityDTO = identityService.getIdentityByCwid(cwid);

		// set first name, middle name, last name, and affiliation.
		TargetAuthor targetAuthor = new TargetAuthor(
				new AuthorName(
						identityDTO.getFirstName(), 
						identityDTO.getMiddleName(), 
						identityDTO.getLastName()),
				new AuthorAffiliation(identityDTO.getPrimaryAffiliation()));

		// set cwid
		targetAuthor.setCwid(identityDTO.getCwid());

		// set known co-investigators.
		List<AuthorName> coinvestigatorAuthorNames = new ArrayList<AuthorName>();
		List<IdentityBean> identityDTOs = identityService.getAssosiatedGrantIdentityList(cwid);
		for (IdentityBean coinvestigatorDTO : identityDTOs) {
			coinvestigatorAuthorNames.add(
					new AuthorName(
							coinvestigatorDTO.getFirstName(), 
							coinvestigatorDTO.getMiddleName(), 
							coinvestigatorDTO.getLastName()));
		}
		targetAuthor.setGrantCoauthors(coinvestigatorAuthorNames);

		// set primary department.
		targetAuthor.setDepartment(identityDTO.getPrimaryDepartment());

		// set other department.
		targetAuthor.setOtherDeparment(identityDTO.getOtherDepartment());

		// set citizenship
		String countryOfCitizenship = identityCitizenshipService.getIdentityCitizenshipCountry(identityDTO.getCwid());
		if (countryOfCitizenship != null) {
			targetAuthor.setCitizenship(countryOfCitizenship);
		}

		// set education degree.
		IdentityDegree identityDegree = identityDegreeService.getIdentityDegreeByCwid(identityDTO.getCwid());
		AuthorDegree authorDegree = IdentityDegreeConverter.convert(identityDegree);
		targetAuthor.setDegree(authorDegree);

		// set author education.
		List<AuthorEducation> authorEducations = identityEducationService.getEducations(cwid);
		targetAuthor.setEducations(authorEducations);

		// Set author institutions.
		List<String> institutions = identityInstitutionService.getInstitutionByCwid(cwid);
		targetAuthor.setInstitutions(institutions);

		BoardCertificationDao boardCertificationDao = new BoardCertificationDaoImpl();
		List<String> boardCertifications = boardCertificationDao.getBoardCertificationsByCwid(identityDTO.getCwid());
		if (!boardCertifications.isEmpty()) {
			targetAuthor.setBoardCertifications(boardCertifications);
		}

		// Merge from gemini
		//Update ReCiter code so that aliases can be included as input #93.
		List<IdentityDirectory> identityDirectoryList = identityDirectoryService.getIdentityDirectoriesByCwid(identityDTO.getCwid());
		List<AuthorName> aliasList = new ArrayList<AuthorName>();
		for (IdentityDirectory identityDirectory : identityDirectoryList) {
			aliasList.add(new AuthorName(identityDirectory.getGivenName(), identityDirectory.getMiddleName(), identityDirectory.getSurname()));
		}
		targetAuthor.setAliasList(aliasList);

		// Set up alternate department names:
		if (targetAuthor.getDepartment() != null) {
			List<String> alternateDepartmentNames = identityAlternateDeptNamesService.getAlternateNames(targetAuthor.getDepartment());
			targetAuthor.setAlternateDepartmentNames(alternateDepartmentNames);
		}

		// Set email and email_other.
		targetAuthor.setEmail(identityDTO.getEmail());
		targetAuthor.setEmailOther(identityDTO.getEmailOther());
		
		// Sponsor Award ids.
		targetAuthor.setSponsorAwardIds(identityGrantService.getSponsorAwardIdListByCwid(targetAuthor.getCwid()));
		
		// set emails from rc_identity_email and combine this information
		// with email and email_other from rc_identity.
		List<String> emailAddresses = identityEmailService.getEmailAddressesForCwid(cwid);
		targetAuthor.setEmailAddresses(emailAddresses);
		
		//		Set<String> terms = constructPubmedQuery(targetAuthor);
		//		String query = getConcatTerms(terms);
		//		String query = getPubMedSearchQuery(targetAuthor.getAuthorName().getLastName(), targetAuthor.getAuthorName().getFirstName());
//		String query = getPubMedSearchQuery(cwid);
//		targetAuthor.setPubmedSearchQuery(query);

		updateMiddleNameFromAlias(targetAuthor);

		return targetAuthor;
	}

	public int buildPubmedQuery(String searchQuery) {
		PubmedESearchHandler pubmedESearchHandler = PubmedESearchHandler.executeESearchQuery(searchQuery);
		return pubmedESearchHandler.getCount();
	}

	/**
	 * In the case where the target author's middle name is an empty string, update the author's middle name from
	 * rc_identity_directory. In the case where there are multiple middle names from rc_identity_directory,
	 * select the middle name with the longest string length.
	 */
	public void updateMiddleNameFromAlias(TargetAuthor targetAuthor) {
		if (targetAuthor.getAuthorName().getMiddleName().length() == 0) {
			int maxLength = 0;
			String maxLengthMiddleName = "";
			List<AuthorName> alias = targetAuthor.getAliasList();
			for (AuthorName authorName : alias) {
				String aliasMiddleName = authorName.getMiddleName();
				if (aliasMiddleName.length() > maxLength) {
					maxLength = aliasMiddleName.length();
					maxLengthMiddleName = aliasMiddleName;
				}
			}
			targetAuthor.getAuthorName().setMiddleName(maxLengthMiddleName);
		}
	}

	public String getPubMedSearchQuery(String cwid) {
		IdentityDao identityDao = new IdentityDaoImpl();
		String query = identityDao.getPubmedQuery(cwid);
		String encodedUrl = null;
		try {
			encodedUrl = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Can be safely ignored because UTF-8 is always supported.
			e.printStackTrace();
		}
		return encodedUrl;
	}

	public String getPubMedSearchQuery(String lastName, String firstName) {
		lastName = lastName.replaceAll(" ", "%20");
		String firstInitial = firstName.substring(0, 1);
		return lastName + "%20" + firstInitial + "[au]";
	}

	public Set<String> constructPubmedQuery(TargetAuthor targetAuthor) {
		Set<String> set = new HashSet<String>();

		String firstName = targetAuthor.getAuthorName().getFirstName();
		String middleName = targetAuthor.getAuthorName().getMiddleName();
		String lastName = targetAuthor.getAuthorName().getLastName();

		PubmedSearchQueryGenerator generator = new PubmedSearchQueryGenerator();
		set.addAll(generator.generate(firstName, middleName, lastName));

		if (targetAuthor.getAliasList() != null) {
			for (AuthorName authorName : targetAuthor.getAliasList()) {
				set.addAll(generator.generate(authorName.getFirstName(), authorName.getMiddleName(), authorName.getLastName()));
			}
		}

		String cwid = targetAuthor.getCwid();
		set.add(cwid + "@nyp.org");
		set.add(cwid + "@med.cornell.edu");
		set.add(cwid + "@med.weill.cornell.edu");

		return set;
	}

	public String getConcatTerms(Set<String> queryTerms) {
		StringBuilder sb = new StringBuilder();
		for (String term : queryTerms) {
			sb.append(term);
			sb.append(" OR ");
		}
		String result = sb.toString();
		if (result.length() > 0) {
			result = result.substring(0, result.length() - " OR ".length());
		}

		result = result.replace(" ", "%20");
		return result;
	}
}
