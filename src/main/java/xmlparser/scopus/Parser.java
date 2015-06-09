package xmlparser.scopus;

import java.io.File;

import xmlparser.scopus.model.ScopusArticle;

public interface Parser {

	ScopusArticle parse(File xmlFile);
}
