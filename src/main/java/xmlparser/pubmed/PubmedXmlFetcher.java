package xmlparser.pubmed;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import xmlparser.AbstractXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;

/**
 * Fetches XML articles from PubMed and writes to a file. One can specify the location
 * where the fetched XML files will go, otherwise a default location will be used based
 * on the configuration setting.
 * 
 * @author jil3004
 *
 */
public class PubmedXmlFetcher extends AbstractXmlFetcher {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractXmlFetcher.class);
	private static final String DEFAULT_LOCATION = "src/main/resources/data/pubmed/";
	private List<String> queries = new ArrayList<String>();;
	private PubmedXmlParser pubmedXmlParser;

	public static String getDefaultLocation() {
		return DEFAULT_LOCATION;
	}
	
	// Create default location if not exist.
	public void createPubMedLocation() {
		File pubmedDir = new File(DEFAULT_LOCATION);
		if (!pubmedDir.exists()) {
			pubmedDir.mkdirs();
		}
	}

	/**
	 * Get all the pubmed articles from PubMed by last name and first initial for a cwid.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 * @return A list of PubmedArticles.
	 */
	public List<PubmedArticle> getPubmedArticle(String lastName, String firstInitial, String middleName, String cwid) {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();

		// Create default location if not exist.
		createPubMedLocation();
		// Create folder for cwid if not exist.
		File cwidDir = new File(getDirectory() + cwid);
		if (!cwidDir.exists()) {
			fetch(lastName, firstInitial, middleName, cwid);
		}
		if(pubmedXmlParser==null)pubmedXmlParser = new PubmedXmlParser(new PubmedEFetchHandler()); // to make sure, the paubmedXmlParser object should not be null
		
		for (File xmlFile : new File(getDirectory() + cwid).listFiles()) {
			pubmedXmlParser.setXmlInputSource(xmlFile);
			List<PubmedArticle> list =pubmedXmlParser.parse();
			if(list!=null)pubmedArticleList.addAll(list);
		}
		return pubmedArticleList;
	}	
	
	//  For each of an author’s aliases, modify initial query based on lexical rules #100 
	public void preparePubMedQueries(String lastName, String firstName, String middleName){
		String firstInitial = "%20" + firstName.substring(0, 1)+ "[au]";
		String middleInitial = "%20" +   (middleName!=null && !middleName.trim().equals("")?middleName.substring(0, 1):"")+ "[au]";
		//  For each of an author’s aliases, modify initial query based on lexical rules #100 
		
		// Circumstance 3. The author’s name has a suffix.
		if(firstName.contains("JR") || firstName.contains("II") || firstName.contains("III")|| firstName.contains("IV")){
			String a = firstName.replace("JR", "");
			a = firstName.replace("II", "");
			a = firstName.replace("III", "");
			a = firstName.replace("IV", "");
			String term = lastName + "%20" +a+"[au]";
			if(!queries.contains(term))queries.add(term);
			term=lastName+"%20"+firstName+"[au]";
			if(!queries.contains(term))queries.add(term);
		}
		
		// Circumstance 4. The author’s last name contains a space or hyphen
		
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
		}
		
		if(lastName.trim().indexOf("-")!=-1){
			String[] lastNameTerms = lastName.split("-");
			String term = lastNameTerms[0]+","+firstInitial; //FirstTermFromLastName, FirstInitial[au]
			if(!queries.contains(term))queries.add(term);
			term=lastNameTerms[0]+"-"+lastNameTerms[lastNameTerms.length-1]+","+firstInitial; //FirstTermFromLastName-LastTermFromLastName, FirstInitial[au]
			if(!queries.contains(term))queries.add(term);
		}
		
		// Circumstance 5. The author’s first name consists of a single letter
		if(firstName.length()==1){
			String term = lastName +firstInitial;//LastName FirstInitial[au] 
			if(!queries.contains(term))queries.add(term);
			term=lastName+middleInitial;//LastName MiddleInitial[au] 
			if(!queries.contains(term))queries.add(term);
			term=lastName+"%20"+(middleName!=null?middleName.substring(0, 1):"")+firstName.substring(0, 1)+ "[au]";//LastName MiddleInitialFirstInitial[au]
			if(!queries.contains(term))queries.add(term);
			term=lastName+"%20"+firstName.substring(0, 1)+(middleName!=null?middleName.substring(0, 1):"")+ "[au]";//LastName FirstInitialMiddleInitial[au]
			if(!queries.contains(term))queries.add(term);
		}
		// 
	}

	/**
	 * Fetch all the publications for this query "lastname firstInitial[au]" in PubMed and store it on disk.
	 * @param lastName last name of the author.
	 * @param firstInitial first initial of the author.
	 * @param cwid cwid of the author.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void fetch(String lastName, String firstName, String middleName, String cwid) {
		int numPubMedArticles = 0;
		File dir = new File(getDirectory() + cwid);
		// Fetch only if directory doesn't exist.
		if (!dir.exists()) {
			preparePubMedQueries(lastName,firstName,middleName);
			for(String query: queries){			
				// Get the count (number of publications for this query).
				PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
				pubmedXmlQuery.setTerm(query);
				numPubMedArticles = numPubMedArticles + fetch(pubmedXmlQuery, cwid, numPubMedArticles);
			}
			slf4jLogger.info("Number of articles retrieved for : " + cwid + " is "+ numPubMedArticles);
		}
	}
	
	/**
	 * Fetch all the publications for the pubmedXmlQuery in PubMed and store it on disk 
	 * @param pubmedXmlQuery
	 * @param cwid
	 * @return numPubMedArticles
	 */
	private int fetch(PubmedXmlQuery pubmedXmlQuery,String cwid, int startPos){
		int numPubMedArticles = -1;
		// set retmax = 1 so that query can be executed fast.
		pubmedXmlQuery.setRetMax(1);

		String eSearchUrl = pubmedXmlQuery.buildESearchQuery();
		PubmedESearchHandler xmlHandler = PubmedESearchHandler.executeESearchQuery(eSearchUrl);
		numPubMedArticles = xmlHandler.getCount();		

		// Retrieve the publications 10,000 records at one time and store to disk.
		int retMax = 10000;
		pubmedXmlQuery.setRetMax(retMax);
		int currentRetStart = 0;

		// Number of partitions that we need to finish retrieving all XML.
		int numSteps = (int) Math.ceil((double)numPubMedArticles / retMax); 

		// Use the retstart value to iteratively fetch all XMLs.
		for (int i = startPos; i < (startPos+numSteps); i++) {
			// Get webenv value.
			pubmedXmlQuery.setRetStart(currentRetStart);
			eSearchUrl = pubmedXmlQuery.buildESearchQuery();
			pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

			// Use the webenv value to retrieve xml.
			String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();

			// Save the xml file to directory data/xml/cwid
			saveXml(eFetchUrl, cwid, cwid + "_" + i);

			// Update the retstart value.
			currentRetStart += pubmedXmlQuery.getRetMax();
			pubmedXmlQuery.setRetStart(currentRetStart);
		}
		return numSteps;
	}

	/**
	 * 
	 * @param filePath
	 * @param expectedResult
	 * @return
	 */
	public boolean isNumberOfArticleMatch(String filePath, int expectedResult) {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();
		for (File xmlFile : new File(filePath).listFiles()) {
			pubmedXmlParser.setXmlInputSource(xmlFile);
			pubmedArticleList.addAll(pubmedXmlParser.parse());
		}
		return pubmedArticleList.size() == expectedResult;
	}

	/**
	 * <p>
	 * Performs a check on a PubMed retrieved XML file for {@code <ERROR>Unable to obtain query #1</ERROR>} on
	 * {@code line 3}. 
	 * </p>
	 * 
	 * <p>
	 * This function might be useful if you need to perform a check on the XML files for this error
	 * message, and perform a re-retrieval if necessary.
	 * </p>
	 * 
	 * @param filePath file location of the PubMed XML file.
	 * @return true if XML file contains the {@code <ERROR>Unable to obtain query #1</ERROR>}, false otherwise.
	 */
	public boolean isUnableToObtainQueryError(String filePath) {
		int lineNumber = 3;
		String lineContent = null;
		String unableToObtainQueryMessage = "<ERROR>Unable to obtain query #1</ERROR>";
		try {
			lineContent = Files.lines(Paths.get(filePath))
					.skip(lineNumber)
					.findFirst()
					.get()
					.trim();
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage());
		}
		return unableToObtainQueryMessage.equals(lineContent);
	}

	public PubmedXmlFetcher() {
		super(DEFAULT_LOCATION);
		pubmedXmlParser = new PubmedXmlParser(new PubmedEFetchHandler());
	}

	public PubmedXmlFetcher(String directory) {
		super(directory);
	}

}
