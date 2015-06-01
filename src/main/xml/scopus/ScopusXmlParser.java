package main.xml.scopus;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.xml.scopus.model.ScopusArticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ScopusXmlParser implements Parser {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlParser.class);
	
	private final InputSource xmlInputSource;
	private final ScopusXmlHandler xmlHandler;
	
	@Override
	public ScopusArticle parse() {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(xmlInputSource, xmlHandler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			slf4jLogger.error(e.getMessage());
		}
		return xmlHandler.getScopusArticle();
	}
	
	public ScopusXmlParser(InputSource xmlInputSource, ScopusXmlHandler xmlHandler) {
		this.xmlInputSource = xmlInputSource;
		this.xmlHandler = xmlHandler;
	}	
}
