package test.scopus.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import main.xml.scopus.ScopusAffiliationHandler;
import main.xml.scopus.ScopusXmlQuery;
import main.xml.scopus.model.ScopusEntry;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ScopusXmlAffiliationHandlerTest {

	@Test
	public void test() throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		// Url for testing:
		// http://api.elsevier.com/content/search/index:SCOPUS?query=pmid(23000000)%20OR%20pmid(24100000)&count=100&field=pubmed-id,affiliation&start=0&view=COMPLETE&apikey=e0fa610418a4859d24f2457e021aea60
		
		ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery();
		List<String> pmidList = new ArrayList<String>();
		pmidList.add("24100000");
		pmidList.add("23000000");
		
		ScopusAffiliationHandler handler = scopusXmlQuery.executeQuery(pmidList);
		
		List<ScopusEntry> scopusEntryList = handler.getScopusEntryList(); 
		assertEquals(2, scopusEntryList.size());
		
		ScopusEntry entry1 = scopusEntryList.get(0);
		assertEquals("24100000", entry1.getPubmedID());
		assertEquals("Uniwersytet Jagiellonski w Krakowie", entry1.getAffiliation().get(0).getAffilName());
		assertEquals("Krakow", entry1.getAffiliation().get(0).getAffiliationCity());
		assertEquals("Poland", entry1.getAffiliation().get(0).getAffiliationCountry());
		assertEquals("Jagellonian University", entry1.getAffiliation().get(0).getNameVariantList().get(0));
		assertEquals("Jagiellonian University", entry1.getAffiliation().get(0).getNameVariantList().get(1));
		
		assertEquals("Uniwersytet Slaski w Katowicach", entry1.getAffiliation().get(1).getAffilName());
		assertEquals("Katowice", entry1.getAffiliation().get(1).getAffiliationCity());
		assertEquals("Poland", entry1.getAffiliation().get(1).getAffiliationCountry());
		assertEquals("Silesian University", entry1.getAffiliation().get(1).getNameVariantList().get(0));
		assertEquals("University of Silesia", entry1.getAffiliation().get(1).getNameVariantList().get(1));
		
		assertEquals("Uniwersytet Jagiellonski w Krakowie Jagellonian University Jagiellonian University Krakow Poland Uniwersytet Slaski w Katowicach Silesian University University of Silesia Katowice Poland ",  entry1.affiliationConcatForm());
		ScopusEntry entry2 = scopusEntryList.get(1);
		assertEquals("23000000", entry2.getPubmedID());
		assertEquals("Monash University", entry2.getAffiliation().get(0).getAffilName());
		assertEquals("Melbourne", entry2.getAffiliation().get(0).getAffiliationCity());
		assertEquals("Australia", entry2.getAffiliation().get(0).getAffiliationCountry());
		assertEquals("Monash University", entry2.getAffiliation().get(0).getNameVariantList().get(0));
		
		assertEquals("University of Newcastle, Australia", entry2.getAffiliation().get(1).getAffilName());
		assertEquals("Callaghan", entry2.getAffiliation().get(1).getAffiliationCity());
		assertEquals("Australia", entry2.getAffiliation().get(1).getAffiliationCountry());
		assertEquals("The University of Newcastle", entry2.getAffiliation().get(1).getNameVariantList().get(0));
		assertEquals("University of Newcastle", entry2.getAffiliation().get(1).getNameVariantList().get(1));
	}
}
