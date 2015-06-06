package xmlparser.pubmed;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import xmlparser.pubmed.model.MedlineCitation;
import xmlparser.pubmed.model.MedlineCitationArticle;
import xmlparser.pubmed.model.MedlineCitationArticleAuthor;
import xmlparser.pubmed.model.MedlineCitationDate;
import xmlparser.pubmed.model.MedlineCitationJournal;
import xmlparser.pubmed.model.MedlineCitationJournalIssue;
import xmlparser.pubmed.model.MedlineCitationKeyword;
import xmlparser.pubmed.model.MedlineCitationKeywordList;
import xmlparser.pubmed.model.MedlineCitationMeshHeading;
import xmlparser.pubmed.model.MedlineCitationMeshHeadingDescriptorName;
import xmlparser.pubmed.model.MedlineCitationPMID;
import xmlparser.pubmed.model.PubmedArticle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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

	private List<PubmedArticle> pubmedArticles;
	private PubmedArticle pubmedArticle;
	
	public List<PubmedArticle> getPubmedArticles() {
		return pubmedArticles;
	}

	/**
	 * Sends a query to the NCBI web site to retrieve a XML document of PubMed articles.
	 * @param webEnv
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static PubmedEFetchHandler executeEFetchQuery(String eFetchUrl) {
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		InputStream inputStream = null;
		try {
			inputStream = new URL(eFetchUrl).openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			SAXParserFactory.newInstance()
				.newSAXParser()
				.parse(inputStream, pubmedXmlHandler);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pubmedXmlHandler;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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
		if (qName.equalsIgnoreCase("PubDate")) {
			pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().setPubDate(new MedlineCitationDate());
			bPubDate = true;
		}
		if (qName.equalsIgnoreCase("ISOAbbreviation")) {
			bJournalISOAbbreviation = true;
		}
		if (bPubDate && qName.equalsIgnoreCase("Year")) {
			bPubDateYear = true;
		}
		if (bPubDate && qName.equalsIgnoreCase("MedlineDate")) {
			bMedlineDate = true;
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
		if (qName.equalsIgnoreCase("LastName")) {
			bAuthorLastName = true;
		}
		if (qName.equalsIgnoreCase("ForeName")) {
			bAuthorForeName = true;
		}
		if (qName.equalsIgnoreCase("Initials")) {
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
	}
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("PubmedArticle")) {
			pubmedArticles.add(pubmedArticle); // add the PubmedArticle to the pubmedArticleList.
		}
		if (qName.equalsIgnoreCase("Article")) {
			bArticle = false;
		}
		if (qName.equalsIgnoreCase("KeywordList")) {
			bKeywordList = false;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (bMedlineCitation && bPMID) {
			String pmid = new String(ch, start, length);
			pubmedArticle.getMedlineCitation().setPmid(new MedlineCitationPMID(pmid)); // set the pmid number of the MedlineCitation.
			bPMID = false;
			bMedlineCitation = false;
		}
		if (bArticle && bArticleTitle) {
			pubmedArticle.getMedlineCitation().getArticle().setArticleTitle(new String(ch, start, length)); // set the title of the Article.
			bArticleTitle = false;
		}
		if (bAuthorLastName) {
			String authorLastName = new String(ch, start, length);
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setLastName(authorLastName);
			bAuthorLastName = false;
		}
		if (bAuthorForeName) {
			String authorForeName = new String(ch, start, length);
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setForeName(authorForeName);
			bAuthorForeName = false;
		}
		if (bAuthorInitials) {
			String authorInitials = new String(ch, start, length);
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setInitials(authorInitials);
			bAuthorInitials = false;
		}
		if (bAffiliation) {
			String affiliation = new String(ch, start, length);
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getArticle().getAuthorList().size() - 1;
			pubmedArticle.getMedlineCitation().getArticle().getAuthorList().get(lastInsertedIndex).setAffiliation(affiliation);
			bAffiliation = false;
		}
		if (bJournalTitle) {
			String journalTitle = new String(ch, start, length);
			pubmedArticle.getMedlineCitation().getArticle().getJournal().setJournalTitle(journalTitle);
			bJournalTitle = false;
		}
		if (bPubDate && bPubDateYear) {
			String pubDateYear = new String(ch, start, length);
			pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().setYear(pubDateYear);
			bPubDate = false;
			bPubDateYear = false;
		}
		if (bJournalISOAbbreviation) {
			pubmedArticle.getMedlineCitation().getArticle().getJournal().setIsoAbbreviation(new String(ch, start, length));
			bJournalISOAbbreviation = false;
		}
		if (bPubDate && bMedlineDate) {
			String pubDateYear = new String(ch, start, length);
			pubDateYear = pubDateYear.substring(0, 4); // PMID = 23849565 <MedlineDate>2013 May-Jun</MedlineDate>
			pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().setYear(pubDateYear);
			bPubDate = false;
			bMedlineDate = false;
		}
		if (bKeywordList && bKeyword) {
			MedlineCitationKeyword keyword = new MedlineCitationKeyword();
			keyword.setKeyword(new String(ch, start, length));
			pubmedArticle.getMedlineCitation().getKeywordList().getKeywordList().add(keyword);
			bKeyword = false;
		}
		if (bDescriptorName) {
			String descriptorName = new String(ch, start, length);
			int lastInsertedIndex = pubmedArticle.getMedlineCitation().getMeshHeadingList().size() - 1;
			pubmedArticle.getMedlineCitation().getMeshHeadingList().get(lastInsertedIndex).getDescriptorName().setDescriptorName(descriptorName); // set descriptor name for MeSH.
			bDescriptorName = false;
		}
	}	
}
