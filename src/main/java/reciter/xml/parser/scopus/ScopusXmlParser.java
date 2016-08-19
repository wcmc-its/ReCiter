package reciter.xml.parser.scopus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import reciter.xml.parser.scopus.model.ScopusArticle;

public class ScopusXmlParser implements Parser {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlParser.class);
	
	private final ScopusXmlHandler xmlHandler;
	
	@Override
	public ScopusArticle parse(File xmlFile) {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(xmlFile, xmlHandler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			slf4jLogger.error(e.getMessage());
		}
		return xmlHandler.getScopusArticle();
	}
	
	public ScopusArticle parse(InputSource inputSource) {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(inputSource, xmlHandler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			slf4jLogger.error(e.getMessage());
		}
		return xmlHandler.getScopusArticle();
	}
	
	public List<ScopusArticle> parseMultiple(InputSource inputSource) {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(inputSource, xmlHandler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			slf4jLogger.error(e.getMessage());
		}
		return xmlHandler.getScopusArticles();
	}
	
	public ScopusXmlParser(ScopusXmlHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}
}
