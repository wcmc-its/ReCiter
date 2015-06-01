package test.scopus.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import main.xml.scopus.Parser;
import main.xml.scopus.ScopusXmlHandler;
import main.xml.scopus.ScopusXmlParser;
import main.xml.scopus.ScopusXmlQuery;
import main.xml.scopus.model.Affiliation;
import main.xml.scopus.model.Author;
import main.xml.scopus.model.ScopusArticle;

import org.junit.Test;
import org.xml.sax.InputSource;

public class ScopusXmlParserTest {

	@Test
	public void testParser() throws MalformedURLException, IOException {
		ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder("25548331").build();
		String scopusUrl = scopusXmlQuery.getQueryUrl();
		InputSource scopusInputSource = new InputSource(new URL(scopusUrl).openStream());
		ScopusXmlHandler scopusXmlHandler = new ScopusXmlHandler();
		Parser scopusParser = new ScopusXmlParser(scopusInputSource, scopusXmlHandler);
		ScopusArticle scopusArticle = scopusParser.parse();
		Map<Integer, Affiliation> affiliations = scopusArticle.getAffiliationMap();
		assertEquals(2, affiliations.size());
		
		Affiliation affiliation60030162 = affiliations.get(60030162);
		assertEquals(60030162, affiliation60030162.getAfid());
		assertEquals("Columbia University in the City of New York", affiliation60030162.getAffilname());
		assertEquals("Columbia University", affiliation60030162.getNameVariant());
		assertEquals("New York", affiliation60030162.getAffiliationCity());
		assertEquals("United States", affiliation60030162.getAffiliationCountry());
		
		Affiliation affiliation60018043 = affiliations.get(60018043);
		assertEquals(60018043, affiliation60018043.getAfid());
		assertEquals("New York Presbyterian Hospital", affiliation60018043.getAffilname());
		assertEquals("New York Presbyterian Hospital", affiliation60018043.getNameVariant());
		assertEquals("New York", affiliation60018043.getAffiliationCity());
		assertEquals("United States", affiliation60018043.getAffiliationCountry());
		
		assertEquals(25548331, scopusArticle.getPubmedId());
		
		Map<Long, Author> authors = scopusArticle.getAuthors();
		assertEquals(10, authors.size());
		
		Author author22958914900 = authors.get(22958914900L);
		assertEquals(1, author22958914900.getSeq());
		assertEquals(22958914900L, author22958914900.getAuthid());
		assertEquals("Stockwell M.", author22958914900.getAuthname());
		assertEquals("Stockwell", author22958914900.getSurname());
		assertEquals("Melissa S.", author22958914900.getGivenName());
		assertEquals("M.S.", author22958914900.getInitials());
		assertEquals(1, author22958914900.getAfidSet().size());
		assertEquals(true, author22958914900.getAfidSet().contains(60030162));
		
		Author author6507210111 = authors.get(6507210111L);
		assertEquals(2, author6507210111.getSeq());
		assertEquals(6507210111L, author6507210111.getAuthid());
		assertEquals("Catallozzi M.", author6507210111.getAuthname());
		assertEquals("Catallozzi", author6507210111.getSurname());
		assertEquals("Marina", author6507210111.getGivenName());
		assertEquals("M.", author6507210111.getInitials());
		assertEquals(1, author6507210111.getAfidSet().size());
		assertEquals(true, author6507210111.getAfidSet().contains(60030162));
		
		Author author55196117900 = authors.get(55196117900L);
		assertEquals(3, author55196117900.getSeq());
		assertEquals(55196117900L, author55196117900.getAuthid());
		assertEquals("Camargo S.", author55196117900.getAuthname());
		assertEquals("Camargo", author55196117900.getSurname());
		assertEquals("Stewin", author55196117900.getGivenName());
		assertEquals("S.", author55196117900.getInitials());
		assertEquals(1, author55196117900.getAfidSet().size());
		assertEquals(true, author55196117900.getAfidSet().contains(60030162));
		
		Author author56149517600 = authors.get(56149517600L);
		assertEquals(4, author56149517600.getSeq());
		assertEquals(56149517600L, author56149517600.getAuthid());
		assertEquals("Ramakrishnan R.", author56149517600.getAuthname());
		assertEquals("Ramakrishnan", author56149517600.getSurname());
		assertEquals("Rajasekhar", author56149517600.getGivenName());
		assertEquals("R.", author56149517600.getInitials());
		assertEquals(1, author56149517600.getAfidSet().size());
		assertEquals(true, author56149517600.getAfidSet().contains(60030162));
		
		Author author55407834700 = authors.get(55407834700L);
		assertEquals(5, author55407834700.getSeq());
		assertEquals(55407834700L, author55407834700.getAuthid());
		assertEquals("Holleran S.", author55407834700.getAuthname());
		assertEquals("Holleran", author55407834700.getSurname());
		assertEquals("Stephen", author55407834700.getGivenName());
		assertEquals("S.", author55407834700.getInitials());
		assertEquals(1, author55407834700.getAfidSet().size());
		assertEquals(true, author55407834700.getAfidSet().contains(60030162));
		
		Author author7004607414 = authors.get(7004607414L);
		assertEquals(6, author7004607414.getSeq());
		assertEquals(7004607414L, author7004607414.getAuthid());
		assertEquals("Findley S.", author7004607414.getAuthname());
		assertEquals("Findley", author7004607414.getSurname());
		assertEquals("Sally E.", author7004607414.getGivenName());
		assertEquals("S.E.", author7004607414.getInitials());
		assertEquals(1, author7004607414.getAfidSet().size());
		assertEquals(true, author7004607414.getAfidSet().contains(60030162));
		
		Author author6603165965 = authors.get(6603165965L);
		assertEquals(7, author6603165965.getSeq());
		assertEquals(6603165965L, author6603165965.getAuthid());
		assertEquals("Kukafka R.", author6603165965.getAuthname());
		assertEquals("Kukafka", author6603165965.getSurname());
		assertEquals("Rita", author6603165965.getGivenName());
		assertEquals("R.", author6603165965.getInitials());
		assertEquals(2, author6603165965.getAfidSet().size());
		assertEquals(true, author6603165965.getAfidSet().contains(60030162));
		assertEquals(true, author6603165965.getAfidSet().contains(60018043));
		
		Author author37077257900 = authors.get(37077257900L);
		assertEquals(8, author37077257900.getSeq());
		assertEquals(37077257900L, author37077257900.getAuthid());
		assertEquals("Hofstetter A.", author37077257900.getAuthname());
		assertEquals("Hofstetter", author37077257900.getSurname());
		assertEquals("Annika M.", author37077257900.getGivenName());
		assertEquals("A.M.", author37077257900.getInitials());
		assertEquals(1, author37077257900.getAfidSet().size());
		assertEquals(true, author37077257900.getAfidSet().contains(60030162));
		
		Author author36866967100 = authors.get(36866967100L);
		assertEquals(9, author36866967100.getSeq());
		assertEquals(36866967100L, author36866967100.getAuthid());
		assertEquals("Fernandez N.", author36866967100.getAuthname());
		assertEquals("Fernandez", author36866967100.getSurname());
		assertEquals("Nadira", author36866967100.getGivenName());
		assertEquals("N.", author36866967100.getInitials());
		assertEquals(1, author36866967100.getAfidSet().size());
		assertEquals(true, author36866967100.getAfidSet().contains(60030162));
		
		Author author9249322500 = authors.get(9249322500L);
		assertEquals(10, author9249322500.getSeq());
		assertEquals(9249322500L, author9249322500.getAuthid());
		assertEquals("Vawdrey D.", author9249322500.getAuthname());
		assertEquals("Vawdrey", author9249322500.getSurname());
		assertEquals("David K.", author9249322500.getGivenName());
		assertEquals("D.K.", author9249322500.getInitials());
		assertEquals(1, author9249322500.getAfidSet().size());
		assertEquals(true, author9249322500.getAfidSet().contains(60030162));
	}

	@Test
	public void testScopusXmlQueryBuilder() {
		ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder("25548331").build();
		String scopusUrl = scopusXmlQuery.getQueryUrl();
		assertEquals(
				"http://api.elsevier.com/content/search/index:SCOPUS?query=pmid(25548331)&count=1&field=pubmed-id,affiliation,author,afid&start=0&view=COMPLETE&apikey=e0fa610418a4859d24f2457e021aea60&httpAccept=application/xml",
				scopusUrl);
	}
}
