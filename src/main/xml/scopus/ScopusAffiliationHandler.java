package main.xml.scopus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.xml.scopus.model.ScopusAffiliation;
import main.xml.scopus.model.ScopusEntry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Scopus XML (search by PMID or DOI) handler.
 * @author jielin
 *
 */
public class ScopusAffiliationHandler extends DefaultHandler {
	
	private List<ScopusEntry> scopusEntryList = new ArrayList<ScopusEntry>();
	private ScopusEntry currentEntry;
	private ScopusAffiliation currentAffiliation;
	private boolean bAffilName;
	private boolean bNameVariant;
	private boolean bPubmedID;
	private boolean bAffiliationCity;
	private boolean bAffiliationCountry;
	
	public static ScopusAffiliationHandler executeAffiliationQuery(String affiliationQuery) {
		SAXParser saxParser = null;
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ScopusAffiliationHandler handler = new ScopusAffiliationHandler();
		try {
			saxParser.parse(affiliationQuery, handler);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return handler;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("entry")) {
			currentEntry = new ScopusEntry();
			currentEntry.setAffiliation(new ArrayList<ScopusAffiliation>());
		}
		if (qName.equalsIgnoreCase("affiliation")) {
			currentAffiliation = new ScopusAffiliation();
			currentAffiliation.setNameVariantList(new ArrayList<String>());
			currentEntry.getAffiliation().add(currentAffiliation);
		}
		if (qName.equalsIgnoreCase("affilname")) {
			bAffilName = true;
		}
		if (qName.equalsIgnoreCase("name-variant")) {
			bNameVariant = true;
		}
		if (qName.equalsIgnoreCase("affiliation-city")) {
			bAffiliationCity = true;
		}
		if (qName.equalsIgnoreCase("affiliation-country")) {
			bAffiliationCountry = true;
		}
		if (qName.equalsIgnoreCase("pubmed-id")) {
			bPubmedID = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("entry")) {
			scopusEntryList.add(currentEntry);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (bAffilName) {
			currentAffiliation.setAffilName(new String(ch, start, length));
			bAffilName = false;
		}
		if (bNameVariant) {
			currentAffiliation.getNameVariantList().add(new String(ch, start, length));
			bNameVariant = false;
		}
		if (bAffiliationCity) {
			currentAffiliation.setAffiliationCity(new String(ch, start, length));
			bAffiliationCity = false;
		}
		if (bAffiliationCountry) {
			currentAffiliation.setAffiliationCountry(new String(ch, start, length));
			bAffiliationCountry = false;
		}
		if (bPubmedID) {
			currentEntry.setPubmedID(new String(ch, start, length));
			bPubmedID = false;
		}
	}

	public List<ScopusEntry> getScopusEntryList() {
		return scopusEntryList;
	}
	
}