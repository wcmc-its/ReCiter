package main.xml.scopus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import main.xml.AbstractXmlFetcher;
import main.xml.pubmed.PubmedXmlFetcher;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.scopus.model.ScopusEntry;

import org.xml.sax.SAXException;

public class ScopusXmlFetcher extends AbstractXmlFetcher {
	
	public ScopusXmlFetcher() {
		super("data/scopus/");
	}
	
	public ScopusXmlFetcher(String directory) {
		super(directory);
	}
	
	/**
	 * Read from disk the Scopus affiliation data for this cwid.
	 * @param lastName Last Name of the person.
	 * @param firstInitial First Initial of the person.
	 * @param cwid CWID of the person.
	 * @return A list of ScopusEntry objects.
	 */
	public List<ScopusEntry> getScopusEntryList(String lastName, String firstInitial, String cwid) {
		List<ScopusEntry> scopusEntryList = new ArrayList<ScopusEntry>();
		
		// Make data/scopus directory if it doesn't exist.
		if (!new File(getDirectory()).exists()) {
			new File(getDirectory()).mkdir();
		}
		
		File dir = new File(getDirectory() + cwid);
		// The affiliations are retrieved when this directory exists. Fetch the affiliations if they do not exist.
		if (!dir.exists()) {
			fetch(lastName, firstInitial, cwid);
		}
		File[] xmlFiles = new File(getDirectory() + cwid).listFiles();
		for (File xmlFile : xmlFiles) {
			ScopusAffiliationHandler handler = 
					ScopusAffiliationHandler.executeAffiliationQuery(xmlFile.getPath());
			scopusEntryList.addAll(handler.getScopusEntryList());
		}
		return scopusEntryList;
	}
	
	/**
	 * Fetch the affiliations for this cwid. Note that the PubmedXmlFetcher must be run first.
	 * @param cwid
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void fetch(String lastName, String firstInitial, String cwid) {
		ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery();
		// Get the PubmedArticles from the disk.
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		
		// Fetch and Parse the PubMed articles if they do not exist in the directory "/data/xml".
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstInitial, cwid); 
		
		// File name counter for storing the xml data.
		int filenameCounter = 0;
		List<String> pmidList = new ArrayList<String>();
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			pmidList.add(pubmedArticle.getMedlineCitation().getPmid().getPmidString());
			// Save the Scopus data every 100 PMIDs.
			if (pmidList.size() == 100) {
				saveXml(scopusXmlQuery.buildAffiliationQuery(pmidList), cwid, cwid + "_" + filenameCounter);
				filenameCounter += 1;
				pmidList.clear();
			}
		}
		// save the remaining PMIDs.
		saveXml(scopusXmlQuery.buildAffiliationQuery(pmidList), cwid, cwid + "_" + filenameCounter);
	}
}
