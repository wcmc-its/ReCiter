package main.reciter.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.article.ReCiterArticleKeywords.Keyword;
import main.reciter.model.author.ReCiterAuthor;
import main.xml.pubmed.PubmedXmlFetcher;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.translator.ArticleTranslator;

public class PythonCSVWriter {

	public static void main(String[] args) {

		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedXmlFetcher.setPerformRetrievePublication(false);
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle("fernandes", "helen", "hef9020");
		// Convert PubmedArticle to ReCiterArticle.
		List<ReCiterArticle> reCiterArticleList = ArticleTranslator.translateAll(pubmedArticleList);

		initTermSet(reCiterArticleList);
		System.out.println(termSet.size());
	}

	private static Set<String> termSet = new HashSet<String>();

	public static void initTermSet(List<ReCiterArticle> reCiterArticleList) {
		for (ReCiterArticle article : reCiterArticleList) {
			String title = article.getArticleTitle().getTitle();
			String[] titleArray = title.split("\\s+");
			for (String t : titleArray) {
				termSet.add(t);
			}

			String journal = article.getJournal().getJournalTitle();
			String[] journalArray = journal.split("\\s+");
			for (String j : journalArray) {
				termSet.add(j);
			}

			for (Keyword keyword : article.getArticleKeywords().getKeywords()) {
				termSet.add(keyword.getKeyword());
			}

			for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
				termSet.add(author.getAuthorName().getCSVFormat());
				if (author.getAffiliation().getAffiliation() != null) {
					String[] affiliationArray = author.getAffiliation().getAffiliation().split("\\s+");
					for (String affiliation : affiliationArray) {
						termSet.add(affiliation);
					}
				}
			}
		}
	}

	public static void write(List<ReCiterArticle> reCiterArticleList) {
		
	}

	public static Set<String> getTermSet() {
		return termSet;
	}

	public static void setTermSet(Set<String> termSet) {
		PythonCSVWriter.termSet = termSet;
	}
}
