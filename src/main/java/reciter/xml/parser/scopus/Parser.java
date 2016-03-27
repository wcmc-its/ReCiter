package reciter.xml.parser.scopus;

import java.io.File;

import reciter.xml.parser.scopus.model.ScopusArticle;

public interface Parser {

	ScopusArticle parse(File xmlFile);
}
