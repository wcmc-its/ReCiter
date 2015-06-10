package xmlparser.scopus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import reciter.algorithm.cluster.ReCiterExample;
import xmlparser.scopus.model.Affiliation;
import xmlparser.scopus.model.Author;
import xmlparser.scopus.model.ScopusArticle;

/**
 * The {@code ScopusXmlHandler} class parses Scopus XML.
 * 
 * @author jil3004
 *
 */
public class ScopusXmlHandler extends DefaultHandler {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlHandler.class);

	private ScopusArticle scopusArticle;

	private boolean bAffiliation;
	private boolean bAfid;
	private boolean bAffilname;
	private boolean bNameVariant;
	private boolean bAffiliationCity;
	private boolean bAffiliationCountry;

	private boolean bPubmedId;

	private boolean bAuthor;
	private boolean bAuthid;
	private boolean bAuthname;
	private boolean bSurname;
	private boolean bGivenName;
	private boolean bInitials;
	private boolean bAfids;
	private boolean bError;

	private int afid;
	private String affilname;
	private String nameVariant;
	private String affiliationCity;
	private String affiliationCountry;
	private Map<Integer, Affiliation> affiliations = new HashMap<Integer, Affiliation>();

	private int pubmedId;

	private int seq;
	private long authid;
	private String authname;
	private String surname;
	private String givenName;
	private String initials;
	private Set<Integer> afids;
	private Map<Long, Author> authors = new HashMap<Long, Author>();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// <affiliation>
		if (qName.equalsIgnoreCase("affiliation")) {
			bAffiliation = true;
		}
		if (bAffiliation) {
			if (qName.equalsIgnoreCase("afid")) {
				bAfid = true;
			}
			if (qName.equalsIgnoreCase("affilname")) {
				bAffilname = true;
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
		}
		// </affiliation>

		// <pubmed-id>
		if (qName.equalsIgnoreCase("pubmed-id")) {
			bPubmedId = true;
		}
		// </pubmed-id>

		// <author>
		if (qName.equalsIgnoreCase("author")) {
			bAuthor = true;
			seq = Integer.parseInt(attributes.getValue("seq"));
			afids = new HashSet<Integer>();
		}
		if (bAuthor) {
			if (qName.equalsIgnoreCase("authid")) {
				bAuthid = true;
			}
			if (qName.equalsIgnoreCase("authname")) {
				bAuthname = true;
			}
			if (qName.equalsIgnoreCase("surname")) {
				bSurname = true;
			}
			if (qName.equalsIgnoreCase("given-name")) {
				bGivenName = true;
			}
			if (qName.equalsIgnoreCase("initials")) {
				bInitials = true;
			}
			if (qName.equalsIgnoreCase("afid")) {
				bAfids = true;
			}
		}
		// </author>

		// <error>
		if (qName.equalsIgnoreCase("error")) {
			bError = true;
		}
		// </error>
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// Only insert a new affiliation if {@code affiliationMap} does not contain an existing {@code afid}
		if (qName.equalsIgnoreCase("affiliation") && bAffiliation) {
			if (bAfid) {
				bAfid = false;
				bAffiliation = false;
			}
			affiliations.put(afid, new Affiliation(afid, affilname, nameVariant, affiliationCity, affiliationCountry));
		}
		if (qName.equalsIgnoreCase("author") && bAuthor) {
			authors.put(authid, new Author(seq, authid, authname, surname, givenName, initials, afids));
		}
		if (qName.equalsIgnoreCase("entry")) {
			if (bError) {
				scopusArticle = null;
			} else {
				scopusArticle = new ScopusArticle(affiliations, pubmedId, authors);
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		if (bAffiliation) {
			if (bAfid) {
				String afidStr = new String(ch, start, length).trim();
				if (afidStr.length() == 0) {
					bAffiliation = false;
					bAfid = false;
				} else {
					try {
						afid = Integer.parseInt(afidStr);
					} catch (NumberFormatException e) {
						slf4jLogger.error(e.getMessage());
						bAfid = false;
						bAffiliation = false;
					}
				}
				if (affiliations.containsKey(afid)) {
					bAffiliation = false; // skip redundant affiliation tag.
				}
				bAfid = false;
			}
		}
		if (bAffilname) {
			affilname = new String(ch, start, length);
			bAffilname = false;
		}
		if (bNameVariant) {
			nameVariant = new String(ch, start, length);
			bNameVariant = false;
		}
		if (bAffiliationCity) {
			affiliationCity = new String(ch, start, length);
			bAffiliationCity = false;
		}
		if (bAffiliationCountry) {
			affiliationCountry = new String(ch, start, length);
			bAffiliationCountry = false;
		}

		if (bPubmedId) {
			pubmedId = Integer.parseInt(new String(ch, start, length));
			bPubmedId = false;
		}

		if (bAuthor) {
			if (bAuthid) {
				authid = Long.parseLong(new String(ch, start, length));
				if (authors.containsKey(authid)) {
					bAuthor = false;
				}
				bAuthid = false;
			}
			if (bAuthname) {
				authname = new String(ch, start, length);
				bAuthname = false;
			}
			if (bSurname) {
				surname = new String(ch, start, length);
				bSurname = false;
			}
			if (bGivenName) {
				givenName = new String(ch, start, length);
				bGivenName = false;
			}
			if (bInitials) {
				initials = new String(ch, start, length);
				bInitials = false;
			}
			if (bAfids) {
				String bAfidStr = new String(ch, start, length).trim();
				if (bAfidStr.length() == 0) {
					bAfids = false;
				} else {
					try {
					afids.add(Integer.parseInt(new String(ch, start, length).trim()));
					} catch (NumberFormatException e) {
						slf4jLogger.error(e.getMessage());
					}
				}
				bAfids = false;
			}
		}
	}

	public ScopusArticle getScopusArticle() {
		return scopusArticle;
	}
}
