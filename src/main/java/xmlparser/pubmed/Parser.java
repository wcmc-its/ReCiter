package xmlparser.pubmed;

import java.util.List;

import xmlparser.pubmed.model.PubmedArticle;

public interface Parser {

	List<PubmedArticle> parse();
}
