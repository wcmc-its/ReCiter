package reciter.xml.parser.scopus;

import java.io.File;

import reciter.model.scopus.ScopusArticle;

public interface Parser {

	ScopusArticle parse(File xmlFile);
}
