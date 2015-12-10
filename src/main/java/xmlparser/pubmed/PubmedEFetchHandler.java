package xmlparser.pubmed;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import xmlparser.pubmed.model.MedlineCitation;
import xmlparser.pubmed.model.MedlineCitationArticle;
import xmlparser.pubmed.model.MedlineCitationArticleAuthor;
import xmlparser.pubmed.model.MedlineCitationDate;
import xmlparser.pubmed.model.MedlineCitationGrant;
import xmlparser.pubmed.model.MedlineCitationJournal;
import xmlparser.pubmed.model.MedlineCitationJournalIssue;
import xmlparser.pubmed.model.MedlineCitationKeyword;
import xmlparser.pubmed.model.MedlineCitationKeywordList;
import xmlparser.pubmed.model.MedlineCitationMeshHeading;
import xmlparser.pubmed.model.MedlineCitationMeshHeadingDescriptorName;
import xmlparser.pubmed.model.MedlineCitationPMID;
import xmlparser.pubmed.model.PubmedArticle;

/**
 * A SAX handler that parses PubMed XML content.
 * 
 * @author jil3004
 *
 */
public class PubmedEFetchHandler extends DefaultHandler {

	private boolean bPubmedArticleSet;
	private boolean bPubmedArticle;
	private boolean bMedlineCitation;
	private boolean bPMID;
	private boolean bDateCreated;
	private boolean bDateCreatedYear;
	private boolean bDateCreatedMonth;
	private boolean bDateCreatedDay;
	private boolean bDateCompleted;
	private boolean bDateCompletedYear;
	private boolean bDateCompletedMonth;
	private boolean bDateCompletedDay;
	private boolean bArticle;
	private boolean bJournal;
	private boolean bISSN;
	private boolean bJournalIssue;
	private boolean bVolume;
	private boolean bIssue;
	private boolean bPubDate;
	private boolean bMedlineDate;
	private boolean bPubDateYear;
	private boolean bPubDateMonth;
	private boolean bJournalTitle;
	private boolean bJournalISOAbbreviation;
	private boolean bArticleTitle;
	private boolean bPagination;
	private boolean bMedlinePgn;
	private boolean bELocationID;
	private boolean bAbstract;
	private boolean bAbstractText;
	private boolean bCopyrightInformation;
	private boolean bAuthorList;
	private boolean bAuthor;
	private boolean bAuthorLastName;
	private boolean bAuthorForeName;
	private boolean bAuthorInitials;
	private boolean bAffiliationInfo;
	private boolean bAffiliation;
	private boolean bPublicationTypeList;
	private boolean bPublicationType;
	private boolean bMedlineJournalInfo;
	private boolean bCountry;
	private boolean bMedlineTA;
	private boolean bNlmUniqueID;
	private boolean bISSNLinking;
	private boolean bCitationSubset;
	private boolean bMeshHeadingList;
	private boolean bMeshHeading;
	private boolean bDescriptorName;
	private boolean bKeywordList;
	private boolean bKeyword;
	private boolean bPubmedData;
	private boolean bHistory;
	private boolean bPubMedPubDate;
	private boolean bPubMedPubDateYear;
	private boolean bPubMedPubDateMonth;
	private boolean bPubMedPubDateDay;
	private boolean bPubMedPubDateHour;
	private boolean bPubMedPubDateMinute;
	private boolean bPublicationStatus;
	private boolean bArticleIdList;
	private boolean bArticleId;
	private boolean bGrantList;
	private boolean bGrant;
	private boolean bGrantId;
	private boolean bGrantAcronym;
	private boolean bGrantAgency;
	private boolean bGrantCountry;

	private List<PubmedArticle> pubmedArticles;
	private List<String> meshHeading;
	private PubmedArticle pubmedArticle;
	
	private StringBuilder chars = new StringBuilder();
	
	public List<String> getMeshHeading() {
		return meshHeading;
	}
	public List<PubmedArticle> getPubmedArticles() {
		return pubmedArticles;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		chars.setLength(0);
		
		if (qName.equalsIgnoreCase("PubmedArticleSet")) {
			pubmedArticles = new ArrayList<PubmedArticle>(); // create a new list of PubmedArticle.
		}
		if (qName.equalsIgnoreCase("PubmedArticle")) {
			pubmedArticle = new PubmedArticle(); // create a new PubmedArticle.
		}
		if (qName.equalsIgnoreCase("MedlineCitation")) {
			pubmedArticle.setMedlineCitation(new MedlineCitation()); // set the PubmedArticle's MedlineCitation.
			bMedlineCitation = true;
		}
		if (qName.equalsIgnoreCase("PMID")) {
			bPMID = true;
		}
		
		if (qName.equalsIgnoreCase("Article")) {
			pubmedArticle.getMedlineCitation().setArticle(new MedlineCitationArticle()); // set the PubmedArticle's MedlineCitation's MedlineCitationArticle.
			bArticle = true;
		}
		
		if (qName.equalsIgnoreCase("ArticleTitle")) {
			bArticleTitle = true;
		}
		
		if (qName.equalsIgnoreCase("Journal")) {
			pubmedArticle.getMedlineCitation().getArticle().setJournal(new MedlineCitationJournal()); // add journal information.
		}
		
		if (qName.equalsIgnoreCase("JournalIssue")) {
			pubmedArticle.getMedlineCitation().getArticle().getJournal().setJournalIssue(new MedlineCitationJournalIssue());
		}
		
		// PubMed XML has either <Year>, <Month>, <Day> tags or <MedlineDate> tag.	
		if (qName.equalsIgnoreCase("PubDate")) {
			pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().setPubDate(new MedlineCitationDate());
			bPubDate = true;
		}
		
		// <Year> tag.
		if (bPubDate && qName.equalsIgnoreCase("Year")) {
			bPubDateYear = true;
		}
		
		// <MedlineDate> tag.
		if (bPubDate && qName.equalsIgnoreCase("MedlineDate")) {
			bMedlineDate = true;
		}
		
		if (qName.equalsIgnoreCase("ISOAbbreviation")) {
			bJournalISOAbbreviation = true;
		}
		
		if (qName.equalsIgnoreCase("Title")) {
			bJournalTitle = true;
		}
		if (qName.equalsIgnoreCase("AuthorList")) {
			pubmedArticle.getMedlineCitation().getArticle().setAuthorList(new ArrayList<MedlineCitationArticleAuthor>()); // set the PubmedArticle's MedlineCitation's MedlineCitationArticle's title.
			bAuthorList = true;
		}
		if (qName.equalsIgnoreCase("Author")) {
			MedlineCitationArticleAuthor author = new MedlineCitationArticleAuthor();
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().add(author); // add author to author list.
			bAuthor = true;
		}
		if (qName.equalsIgnoreCase("LastName") && bAuthorList) {
			bAuthorLastName = true;
		}
		if (qName.equalsIgnoreCase("ForeName") && bAuthorList) {
			bAuthorForeName = true;
		}
		if (qName.equalsIgnoreCase("Initials") && bAuthorList) {
			bAuthorInitials = true;
		}
		if (qName.equalsIgnoreCase("Affiliation")) {
			bAffiliation = true;
		}
		if (qName.equalsIgnoreCase("KeywordList")) {
			pubmedArticle.getMedlineCitation().setKeywordList(new MedlineCitationKeywordList()); // add keyword information.
			pubmedArticle.getMedlineCitation().getKeywordList().setKeywordList(new ArrayList<MedlineCitationKeyword>());
			bKeywordList = true;
		}
		if (qName.equalsIgnoreCase("Keyword")) {
			bKeyword = true;
		}
		if (qName.equalsIgnoreCase("MeshHeadingList")) {
			pubmedArticle.getMedlineCitation().setMeshHeadingList(new ArrayList<MedlineCitationMeshHeading>());
		}
		if (qName.equalsIgnoreCase("DescriptorName")) {
			MedlineCitationMeshHeading medlineCitationMeshHeading = new MedlineCitationMeshHeading();
			MedlineCitationMeshHeadingDescriptorName medlineCitationMeshHeadingDescriptorName = new MedlineCitationMeshHeadingDescriptorName();
			medlineCitationMeshHeading.setDescriptorName(medlineCitationMeshHeadingDescriptorName);
			pubmedArticle.getMedlineCitation().getMeshHeadingList().add(medlineCitationMeshHeading);
			bDescriptorName = true;
		}
		if (qName.equalsIgnoreCase("GrantList")) {
			pubmedArticle.getMedlineCitation().getArticle().setGrantList(new ArrayList<MedlineCitationGrant>());
			bGrantList = true;
		}
		if (qName.equalsIgnoreCase("Grant")) {
			MedlineCitationGrant grant = new MedlineCitationGrant();
			pubmedArticle.getMedlineCitation().getArticle().getGrantList().add(grant);
			bGrant = true;
		}
		if (qName.equalsIgnoreCase("GrantID")) {
			bGrantId = true;
		}
		if (qName.equalsIgnoreCase("Acronym")) {
			bGrantAcronym = true;
		}
		if (qName.equalsIgnoreCase("Agency")) {
			bGrantAgency = true;
		}
		if (qName.equalsIgnoreCase("Country")) {
			bGrantCountry = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		// PMID
		if (bMedlineCitation && bPMID) {
			String pmid = chars.toString();
			pubmedArticle.getMedlineCitation().setPmid(new MedlineCitationPMID(pmid));
			bPMID = false;
			bMedlineCitation = false;
		}
		
		// Article title.
		if (bArticle && bArticleTitle) {
			String articleTitle = chars.toString();
			pubmedArticle.getMedlineCitation().getArticle().setArticleTitle(articleTitle); // set the title of the Article.
			bArticleTitle = false;
		}
		
		// Author last name.
		if (bAuthorLastName) {
			String authorLastName = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setLastName(authorLastName);
			bAuthorLastName = false;
		}
		
		// Author fore name.
		if (bAuthorForeName) {
			String authorForeName = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setForeName(authorForeName);
			bAuthorForeName = false;
		}
		
		// Author middle initials.
		if (bAuthorInitials) {
			String authorInitials = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setInitials(authorInitials);
			bAuthorInitials = false;
		}
		
		// Author affiliations.
		if (bAffiliation) {
			String affiliation = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setAffiliation(affiliation);
			bAffiliation = false;
		}
		
		// Journal title
		if (bJournalTitle) {
			String journalTitle = chars.toString();
			pubmedArticle.getMedlineCitation().getArticle().getJournal().setJournalTitle(journalTitle);
			bJournalTitle = false;
		}
		
		// Journal ISO abbreviation.
		if (bJournalISOAbbreviation) {
			String isoAbbr = chars.toString();
			pubmedArticle.getMedlineCitation().getArticle().getJournal().setIsoAbbreviation(isoAbbr);
			bJournalISOAbbreviation = false;
		}
		
		// Journal Year.
		if (bPubDate && bPubDateYear) {
			String pubDateYear = chars.toString();
			pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().setYear(pubDateYear);
			bPubDate = false;
			bPubDateYear = false;
		}
		
		// Journal MedlineDate.
		if (bPubDate && bMedlineDate) {
			String pubDateYear = chars.toString();
			if (pubDateYear.length() > 4) {
				pubDateYear = pubDateYear.substring(0, 4); // PMID = 23849565 <MedlineDate>2013 May-Jun</MedlineDate>
			}
			pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().setYear(pubDateYear);
			bPubDate = false;
			bMedlineDate = false;
		}
		
		// Keyword.
		if (bKeywordList && bKeyword) {
			MedlineCitationKeyword keyword = new MedlineCitationKeyword();
			keyword.setKeyword(chars.toString());
			pubmedArticle.getMedlineCitation().getKeywordList().getKeywordList().add(keyword);
			bKeyword = false;
		}
		
		// MeSH descriptor name.
		if (bDescriptorName) {
			String descriptorName = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getMeshHeadingList().size() - 1;
			pubmedArticle.getMedlineCitation().getMeshHeadingList().get(lastInsertedIndex).getDescriptorName().setDescriptorName(descriptorName); // set descriptor name for MeSH.
			bDescriptorName = false;
		}
		
		// End of PubmedArticle tag. Add the PubmedArticle to the pubmedArticleList.
		if (qName.equalsIgnoreCase("PubmedArticle")) {
			pubmedArticles.add(pubmedArticle);
		}
		
		// End of Article tag.
		if (qName.equalsIgnoreCase("Article")) {
			bArticle = false;
		}
		
		// End of keyword list.
		if (qName.equalsIgnoreCase("KeywordList")) {
			bKeywordList = false;
		}
		
		// End of GrantID tag.
		if (bGrant && bGrantId) {
			String grantId = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getGrantList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getGrantList().get(lastInsertedIndex).setGrantID(grantId);
			bGrantId = false;
		}
		
		if (bGrant && bGrantAcronym) {
			String grantAcronym = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getGrantList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getGrantList().get(lastInsertedIndex).setAcronym(grantAcronym);
			bGrantAcronym = false;
		}
		
		if (bGrant && bGrantAgency) {
			String grantAgency = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getGrantList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getGrantList().get(lastInsertedIndex).setAgency(grantAgency);
			bGrantAgency = false;
		}
		
		if (bGrant && bGrantCountry) {
			String grantCountry = chars.toString();
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getGrantList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getGrantList().get(lastInsertedIndex).setCountry(grantCountry);
			bGrantCountry = false;
		}
		
		if (qName.equalsIgnoreCase("Grant")) {
			bGrant = false;
		}
		
		if (qName.equalsIgnoreCase("GrantList")) {
			bGrantList = false;
		}
		
		if (qName.equalsIgnoreCase("AuthorList")) {
			bAuthorList = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	    
		if (bMedlineCitation && bPMID) {
			chars.append(ch, start, length);		
		}
		
		if (bArticle && bArticleTitle) {
			chars.append(ch, start, length);
		}
		
		if (bAuthorLastName) {
			chars.append(ch, start, length);
		}
		
		if (bAuthorForeName) {
			chars.append(ch, start, length);
		}
		
		if (bAuthorInitials) {
			chars.append(ch, start, length);
		}
		
		if (bAffiliation) {
			chars.append(ch, start, length);
		}
		
		if (bJournalTitle) {
			chars.append(ch, start, length);
		}
		
		if (bJournalISOAbbreviation) {
			chars.append(ch, start, length);
		}
		
		if (bPubDate && bPubDateYear) {
			chars.append(ch, start, length);
		}
		
		if (bPubDate && bMedlineDate) {
			chars.append(ch, start, length);
		}
		
		if (bKeywordList && bKeyword) {
			chars.append(ch, start, length);
		}
		
		if (bDescriptorName) {
			chars.append(ch, start, length);
		}
		
		if (bGrant && bGrantId) {
			chars.append(ch, start, length);
		}
		
		if (bGrant && bGrantAcronym) {
			chars.append(ch, start, length);
		}
		
		if (bGrant && bGrantAgency) {
			chars.append(ch, start, length);
		}
		
		if (bGrant && bGrantCountry) {
			chars.append(ch, start, length);
		}
	}	
}