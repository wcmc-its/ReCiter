package reciter.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import database.dao.BoardCertificationDao;
import database.dao.IdentityAlternateDeptNamesDao;
import database.dao.IdentityCitizenshipDao;
import database.dao.IdentityDao;
import database.dao.IdentityDegreeDao;
import database.dao.IdentityDirectoryDao;
import database.dao.IdentityInstitutionDao;
import database.dao.impl.BoardCertificationDaoImpl;
import database.dao.impl.IdentityAlternateDeptNamesDaoImpl;
import database.dao.impl.IdentityCitizenshipDaoImpl;
import database.dao.impl.IdentityDaoImpl;
import database.dao.impl.IdentityDegreeDaoImpl;
import database.dao.impl.IdentityDirectoryDaoImpl;
import database.dao.impl.IdentityInstitutionDaoImpl;
import database.model.IdentityDegree;
import database.model.IdentityDirectory;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorDegree;
import reciter.model.author.AuthorEducation;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import reciter.service.IdentityEducationService;
import reciter.service.IdentityService;
import reciter.service.TargetAuthorService;
import reciter.service.converters.IdentityDegreeConverter;
import reciter.service.dto.IdentityDTO;
import reciter.string.PubmedSearchQueryGenerator;
import xmlparser.pubmed.PubmedESearchHandler;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.PubmedXmlQuery;

public class TargetAuthorServiceImpl implements TargetAuthorService {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(TargetAuthorServiceImpl.class);

	@Override
	public TargetAuthor getTargetAuthor(String cwid) {
		IdentityService identityService = new IdentityServiceImpl();
		IdentityDTO identityDTO = identityService.getIdentityByCwid(cwid);

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
		List<IdentityDTO> identityDTOs = identityService.getAssosiatedGrantIdentityList(cwid);
		for (IdentityDTO coinvestigatorDTO : identityDTOs) {
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
		IdentityCitizenshipDao identityCitizenshipDao = new IdentityCitizenshipDaoImpl();
		String countryOfCitizenship = identityCitizenshipDao.getIdentityCitizenshipCountry(identityDTO.getCwid());
		if (countryOfCitizenship != null) {
			targetAuthor.setCitizenship(countryOfCitizenship);
		}

		// set education degree.
		IdentityDegreeDao identityDegreeDao = new IdentityDegreeDaoImpl();
		IdentityDegree identityDegree = identityDegreeDao.getIdentityDegreeByCwid(identityDTO.getCwid());
		AuthorDegree authorDegree = IdentityDegreeConverter.convert(identityDegree);
		targetAuthor.setDegree(authorDegree);

		// set author education.
		IdentityEducationService identityEducationService = new IdentityEducationServiceImpl();
		List<AuthorEducation> authorEducations = identityEducationService.getEducations(cwid);
		targetAuthor.setEducations(authorEducations);

		// Set author institutions.
		IdentityInstitutionDao identityInstitutionDao = new IdentityInstitutionDaoImpl();
		List<String> institutions = identityInstitutionDao.getInstitutionByCwid(cwid);
		targetAuthor.setInstitutions(institutions);

		BoardCertificationDao boardCertificationDao = new BoardCertificationDaoImpl();
		List<String> boardCertifications = boardCertificationDao.getBoardCertificationsByCwid(identityDTO.getCwid());
		if (!boardCertifications.isEmpty()) {
			targetAuthor.setBoardCertifications(boardCertifications);
		}

		// Merge from gemini
		//Update ReCiter code so that aliases can be included as input #93.
		IdentityDirectoryDao dao = new IdentityDirectoryDaoImpl();
		List<IdentityDirectory> identityDirectoryList = dao.getIdentityDirectoriesByCwid(identityDTO.getCwid());
		List<AuthorName> aliasList = new ArrayList<AuthorName>();
		for (IdentityDirectory identityDirectory : identityDirectoryList) {
			aliasList.add(new AuthorName(identityDirectory.getGivenName(), identityDirectory.getMiddleName(), identityDirectory.getSurname()));
		}
		targetAuthor.setAliasList(aliasList);

		// Set up alternate department names:
		if (targetAuthor.getDepartment() != null) {
			IdentityAlternateDeptNamesDao identityAlternateDeptNamesDao = new IdentityAlternateDeptNamesDaoImpl();
			List<String> alternateDepartmentNames = identityAlternateDeptNamesDao.getAlternateNames(targetAuthor.getDepartment());
			targetAuthor.setAlternateDepartmentNames(alternateDepartmentNames);
		}

		// Set email and email_other.
		targetAuthor.setEmail(identityDTO.getEmail());
		targetAuthor.setEmailOther(identityDTO.getEmailOther());

		//		Set<String> terms = constructPubmedQuery(targetAuthor);
		//		String query = getConcatTerms(terms);
		//		String query = getPubMedSearchQuery(targetAuthor.getAuthorName().getLastName(), targetAuthor.getAuthorName().getFirstName());
		String query = getPubMedSearchQuery(cwid);
		targetAuthor.setPubmedSearchQuery(query);

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
