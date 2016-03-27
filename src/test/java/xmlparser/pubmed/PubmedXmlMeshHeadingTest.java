package xmlparser.pubmed;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.model.MedlineCitationMeshHeading;
import reciter.xml.parser.pubmed.model.MedlineCitationMeshHeadingQualifierName;
import reciter.xml.parser.pubmed.model.MedlineCitationYNEnum;
import reciter.xml.parser.pubmed.model.PubmedArticle;

public class PubmedXmlMeshHeadingTest {

	private String location = "src/test/resources/xml/";
	private String fileName = "21740463.xml";
	private List<MedlineCitationMeshHeading> meshHeadingList;
	
	@Before
	public void setUp() throws Exception {
		File file = new File(location + fileName);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(is, pubmedXmlHandler);
		
		PubmedArticle article = pubmedXmlHandler.getPubmedArticles().get(0);
		meshHeadingList = article.getMedlineCitation().getMeshHeadingList();
	}
	
	@Test
	public void testSizeOfMeshHeadingList() {
		assertEquals(12, meshHeadingList.size());
	}
	
	@Test
	public void testFirstMeshHeading() {
		MedlineCitationMeshHeading meshHeading = meshHeadingList.get(0);
		assertEquals("Adult", meshHeading.getDescriptorName().getDescriptorNameString());
		assertEquals(MedlineCitationYNEnum.N, meshHeading.getDescriptorName().getMajorTopicYN());
	}

	@Test
	public void testLasttMeshHeading() {
		MedlineCitationMeshHeading meshHeading = meshHeadingList.get(meshHeadingList.size() - 1);
		assertEquals("Young Adult", meshHeading.getDescriptorName().getDescriptorNameString());
		assertEquals(MedlineCitationYNEnum.N, meshHeading.getDescriptorName().getMajorTopicYN());
	}
	
	@Test
	public void testSizeOfQualifierNameList() {
		MedlineCitationMeshHeading meshHeading = meshHeadingList.get(1);
		assertEquals(2, meshHeading.getQualifierNameList().size());
	}
	
	@Test
	public void testQualifierName() {
		MedlineCitationMeshHeading meshHeading = meshHeadingList.get(1);
		List<MedlineCitationMeshHeadingQualifierName> qualifierNames = meshHeading.getQualifierNameList();
		assertEquals("etiology", qualifierNames.get(0).getQualifierName());
		assertEquals("psychology", qualifierNames.get(1).getQualifierName());
		assertEquals(MedlineCitationYNEnum.Y, qualifierNames.get(0).getMajorTopicYN());
		assertEquals(MedlineCitationYNEnum.N, qualifierNames.get(1).getMajorTopicYN());
	}
}
