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
		// end </affiliation> tag.

		// <pubmed-id>
		if (qName.equalsIgnoreCase("pubmed-id")) {
			bPubmedId = true;
		}
		// end </pubmed-id> tag.

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
		// end </author> tag.

		// <error>
		if (qName.equalsIgnoreCase("error")) {
			bError = true;
		}
		// end </error> tag.
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		if (bAffiliation) {
			if (bAfid) {
				afid = Integer.parseInt(new String(ch, start, length));
			}
			if (bAffilname) {
				affilname = new String(ch, start, length);
			}
			if (bNameVariant) {
				nameVariant = new String(ch, start, length);
			}
			if (bAffiliationCity) {
				affiliationCity = new String(ch, start, length);
			}
			if (bAffiliationCountry) {
				affiliationCountry = new String(ch, start, length);
			}
		}

		if (bPubmedId) {
			pubmedId = Integer.parseInt(new String(ch, start, length));
		}

		if (bAuthor) {
			if (bAuthid) {
				authid = Long.parseLong(new String(ch, start, length));
			}
			if (bAuthname) {
				authname = new String(ch, start, length);
			}
			if (bSurname) {
				surname = new String(ch, start, length);
			}
			if (bGivenName) {
				givenName = new String(ch, start, length);
			}
			if (bInitials) {
				initials = new String(ch, start, length);
			}
			if (bAfids) {
				int afid = Integer.parseInt(new String(ch, start, length));
				if (afid != 0) {
					afids.add(afid);
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (qName.equalsIgnoreCase("affiliation") && bAffiliation) {
			if (afid != 0) {
				affiliations.put(afid, new Affiliation(afid, affilname, nameVariant, affiliationCity, affiliationCountry));
			}
			bAffiliation = false;
		}

		// <affiliation> child tags need to be checked for empty contents.
		// Check for empty XML tags: ie: <afid />
		if (bAffiliation) {
			if (qName.equalsIgnoreCase("afid")) {
				bAfid = false;
			}
			if (qName.equalsIgnoreCase("affilname")) {
				bAffilname = false;
			}
			if (qName.equalsIgnoreCase("name-variant")) {
				bNameVariant = false;
			}
			if (qName.equalsIgnoreCase("affiliation-city")) {
				bAffiliationCity = false;
			}
			if (qName.equalsIgnoreCase("affiliation-country")) {
				bAffiliationCountry = false;
			}
		}

		if (qName.equalsIgnoreCase("pubmed-id")) {
			bPubmedId = false;
		}

		if (qName.equalsIgnoreCase("author") && bAuthor) {
			if (authid != 0) {
				authors.put(authid, new Author(seq, authid, authname, surname, givenName, initials, afids));
			}
			bAuthor = false;
		}

		if (bAuthor) {
			if (qName.equalsIgnoreCase("authid")) {
				bAuthid = false;
			}
			if (qName.equalsIgnoreCase("authname")) {
				bAuthname = false;
			}
			if (qName.equalsIgnoreCase("surname")) {
				bSurname = false;
			}
			if (qName.equalsIgnoreCase("given-name")) {
				bGivenName = false;
			}
			if (qName.equalsIgnoreCase("initials")) {
				bInitials = false;
			}
			if (qName.equalsIgnoreCase("afid")) {
				bAfids = false;
			}
		}

		// Check for error entry. Return null.
		if (qName.equalsIgnoreCase("entry")) {
			if (bError) {
				scopusArticle = null;
			} else {
				scopusArticle = new ScopusArticle(affiliations, pubmedId, authors);
			}
		}
	}

	public ScopusArticle getScopusArticle() {
		return scopusArticle;
	}
}
