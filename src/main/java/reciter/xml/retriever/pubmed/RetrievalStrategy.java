package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reciter.model.author.TargetAuthor;

public interface RetrievalStrategy {

	void retrieve(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException;
}
