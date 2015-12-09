package reciter.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.BoardCertificationDao;
import database.dao.IdentityCitizenshipDao;
import database.dao.IdentityDegreeDao;
import database.dao.IdentityDirectoryDao;
import database.dao.impl.BoardCertificationDaoImpl;
import database.dao.impl.IdentityCitizenshipDaoImpl;
import database.dao.impl.IdentityDegreeDaoImpl;
import database.dao.impl.IdentityDirectoryDaoImpl;
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
import xmlparser.AbstractXmlFetcher;

public class TargetAuthorServiceImpl implements TargetAuthorService {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractXmlFetcher.class);
	
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

		BoardCertificationDao boardCertificationDao = new BoardCertificationDaoImpl();
		List<String> boardCertifications = boardCertificationDao.getBoardCertificationsByCwid(identityDTO.getCwid());
		if (!boardCertifications.isEmpty()) {
			targetAuthor.setBoardCertifications(boardCertifications);
		}

		// Merge from gemini
		//Update ReCiter code so that aliases can be included as input #93.
		IdentityDirectoryDao dao = new IdentityDirectoryDaoImpl();
		List<IdentityDirectory> identityDirectoryList = dao.getIdentityDirectoriesByCwid(identityDTO.getCwid());
		targetAuthor.setAliasList(identityDirectoryList);
		
		// Set email and email_other.
		targetAuthor.setEmail(identityDTO.getEmail());
		targetAuthor.setEmailOther(identityDTO.getEmailOther());
		
		return targetAuthor;
	}
	
	public String getPubMedSearchQuery(String lastName, String firstName) {
		lastName = lastName.replaceAll(" ", "%20");
		String firstInitial = firstName.substring(0, 1);
		return lastName + "%20" + firstInitial + "[au]";
	}
	
	public String constructPubmedQuery(TargetAuthor targetAuthor) {
		String firstName = targetAuthor.getAuthorName().getFirstInitial();
		String lastName = targetAuthor.getAuthorName().getLastName();
		
		return null;
	}
	
//  For each of an author's aliases, modify initial query based on lexical rules #100 
	public List<String> preparePubMedQueries(String lastName, String firstName, String middleName){
		List<String> queries = new ArrayList<String>();
		
		String firstInitial = "%20" + firstName.substring(0, 1)+ "[au]";
		String middleInitial = "%20" +   (middleName!=null && !middleName.trim().equals("")?middleName.substring(0, 1):"")+ "[au]";
		//  For each of an author�셲 aliases, modify initial query based on lexical rules #100 

		// Circumstance 3. The author�셲 name has a suffix.
		if(firstName.contains("JR") || firstName.contains("II") || firstName.contains("III")|| firstName.contains("IV")){

			String a = firstName.replace("JR", "");
			a = firstName.replace("II", "");
			a = firstName.replace("III", "");
			a = firstName.replace("IV", "");
			String term = lastName + "%20" +a+"[au]";
			if(!queries.contains(term))queries.add(term);
			term=lastName+"%20"+firstName+"[au]";
			if(!queries.contains(term))queries.add(term);
			slf4jLogger.info(" Querry Modified to meet issue100 circumstance 3");
		}

		// Circumstance 4. The author�셲 last name contains a space or hyphen

		queries.add(lastName.replaceAll(" ", "%20") + firstInitial); 
		if(lastName.trim().indexOf(" ")!=-1){
			String[] lastNameTerms = lastName.split(" ");
			String term = lastName.replaceAll(" ", "-") + firstInitial;
			if(!queries.contains(term))queries.add(term);

			term = lastNameTerms[0]+","+firstInitial; //FirstTermFromLastName, FirstInitial[au]
			if(!queries.contains(term))queries.add(term);
			term=lastNameTerms[0]+"-"+lastNameTerms[lastNameTerms.length-1]+","+firstInitial; //FirstTermFromLastName-LastTermFromLastName, FirstInitial[au]
			if(!queries.contains(term))queries.add(term);
			term=lastNameTerms[0]+"%20"+lastNameTerms[lastNameTerms.length-1]+","+firstInitial; //FirstTermFromLastName LastTermFromLastName, FirstInitial[au]
			slf4jLogger.info(" Querry Modified to meet issue100 circumstance 4");
		}

		if(lastName.trim().indexOf("-")!=-1){
			String[] lastNameTerms = lastName.split("-");
			String term = lastNameTerms[0]+","+firstInitial; //FirstTermFromLastName, FirstInitial[au]
			if(!queries.contains(term))queries.add(term);
			term=lastNameTerms[0]+"-"+lastNameTerms[lastNameTerms.length-1]+","+firstInitial; //FirstTermFromLastName-LastTermFromLastName, FirstInitial[au]
			if(!queries.contains(term))queries.add(term);
		}

		// Circumstance 5. The author�셲 first name consists of a single letter
		if(firstName.length()==1){
			String term = lastName +firstInitial;//LastName FirstInitial[au] 
			if(!queries.contains(term))queries.add(term);
			term=lastName+middleInitial;//LastName MiddleInitial[au] 
			if(!queries.contains(term))queries.add(term);
			term=lastName+"%20"+(middleName!=null?middleName.substring(0, 1):"")+firstName.substring(0, 1)+ "[au]";//LastName MiddleInitialFirstInitial[au]
			if(!queries.contains(term))queries.add(term);
			term=lastName+"%20"+firstName.substring(0, 1)+(middleName!=null?middleName.substring(0, 1):"")+ "[au]";//LastName FirstInitialMiddleInitial[au]
			if(!queries.contains(term))queries.add(term);
			slf4jLogger.info(" Querry Modified to meet issue100 circumstance 5");
		}
		
		return queries;
	}
}
