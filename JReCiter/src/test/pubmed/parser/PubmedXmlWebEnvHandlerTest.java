package test.pubmed.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import main.xml.pubmed.PubmedEFetchHandler;
import main.xml.pubmed.PubmedESearchHandler;
import main.xml.pubmed.model.PubmedArticle;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PubmedXmlWebEnvHandlerTest {

	private static final String ESEARCH = "http://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=1&usehistory=y&term=";
	private static final String WEB_ENV = "http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?retmode=xml&db=pubmed&query_key=1&WebEnv=";
	
	@Test
	public void test() throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		PubmedESearchHandler webEnvHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(ESEARCH + "Wang%20Y[au]").openStream();
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(esearchStream, webEnvHandler);
		
		System.out.println("The web env is: " + webEnvHandler.getWebEnv());
		System.out.println("The article count is: " + webEnvHandler.getCount());
		
		System.exit(0);
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		String webEnv = webEnvHandler.getWebEnv();
		InputSource webEnvStream = new InputSource(new URL(WEB_ENV + webEnv).openStream());
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(webEnvStream, pubmedXmlHandler);
		
		List<PubmedArticle> articleList = pubmedXmlHandler.getPubmedArticles();
		int i = 1;
		for (PubmedArticle article : articleList) {
			System.out.println(i + ": Pmid is: " + article.getMedlineCitation().getPmid().getPmidString());
			i++;
		}
	}
}
