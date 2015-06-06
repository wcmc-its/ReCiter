package reciter.lucene;

import java.util.ArrayList;
import java.util.List;

import reciter.model.article.ReCiterArticle;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;

public class DocumentTranslator {

	public static List<Document> translateAll(List<ReCiterArticle> reCiterArticleList) {
		List<Document> documentList = new ArrayList<Document>();
		for (ReCiterArticle reCiterArticle : reCiterArticleList) {
			documentList.add(translate(reCiterArticle));
		}
		return documentList;
	}

	/**
	 * Converts a ReCiterArticle to a Lucene Document.
	 * @param reCiterArticle a ReCiterArticle object.
	 * @return a Lucene Document which contains Fields corresponding to the fields of the ReCiterArticle object.
	 */
	public static Document translate(ReCiterArticle reCiterArticle) {

		// Create a new Lucene Document.
		Document document = new Document();

		// Untokenized FieldType
		FieldType untokenizedFieldType = new FieldType(StringField.TYPE_STORED);
		untokenizedFieldType.setStoreTermVectors(true); // need to keep this b/c it stores the vector?

		// Tokenized FieldType.
		FieldType tokenizedFieldType = new FieldType();
		tokenizedFieldType.setIndexed(true);
		tokenizedFieldType.setStored(true);
		tokenizedFieldType.setStoreTermVectors(true);
		tokenizedFieldType.setOmitNorms(false);

		// Add PMID field.
		document.add(new Field(DocumentVectorType.PMID.name(), Integer.toString(reCiterArticle.getArticleID()), untokenizedFieldType));

		// Add article title field.
		if (reCiterArticle.getArticleTitle() != null) {

			// Untokenized. ie: title is not split by whitespace, but kept as an entire string.
			document.add(new Field(DocumentVectorType.ARTICLE_TITLE_UNTOKENIZED.name(), reCiterArticle.getArticleTitle().getTitle(), untokenizedFieldType));

			// Tokenized.
			document.add(new Field(DocumentVectorType.ARTICLE_TITLE.name(), reCiterArticle.getArticleTitle().getTitle(), tokenizedFieldType));
		}

		// Add journal title field.
		if (reCiterArticle.getJournal() != null) {

			// Untokenized.
			document.add(new Field(DocumentVectorType.JOURNAL_TITLE_UNTOKENIZED.name(), reCiterArticle.getJournal().getJournalTitle(), untokenizedFieldType));

			// Tokenized.
			document.add(new Field(DocumentVectorType.JOURNAL_TITLE.name(), reCiterArticle.getJournal().getJournalTitle(), tokenizedFieldType));
		}

		// Add keywords field. (Tokenized) Stored as keywords separated by space.
		if (reCiterArticle.getArticleKeywords() != null) {

			// Untokenized.
			document.add(new Field(DocumentVectorType.KEYWORD_UNTOKENIZED.name(), reCiterArticle.getArticleKeywords().getCommaConcatForm(), untokenizedFieldType));

			// Tokenized.
			document.add(new Field(DocumentVectorType.KEYWORD.name(), reCiterArticle.getArticleKeywords().getConcatForm(), tokenizedFieldType));
		}

		// Add Author field.
		if (reCiterArticle.getArticleCoAuthors() != null) {
			int numAuthors = reCiterArticle.getArticleCoAuthors().getNumberCoAuthors();
			document.add(new Field(DocumentVectorType.AUTHOR_SIZE.name(), Integer.toString(numAuthors), tokenizedFieldType));

			for (int i = 0; i < numAuthors; i++) {
				// Author Field names are incremented by "_i" for each author in the article.
				String indexableName = reCiterArticle.getArticleCoAuthors().getCoAuthors().get(i).getAuthorName().getLuceneIndexableFormat();
				document.add(new Field(DocumentVectorType.AUTHOR.name() + "_" + i, indexableName, untokenizedFieldType));
			}
		}

		// Add affiliations field.
		if (reCiterArticle.getArticleCoAuthors() != null) {
			String concat = reCiterArticle.getArticleCoAuthors().getAffiliationConcatForm();
			if (concat.length() != 0) {
				document.add(new Field(DocumentVectorType.AFFILIATION.name(), concat, tokenizedFieldType));
			}

			String concatFormWithComma = reCiterArticle.getArticleCoAuthors().getAffiliationConcatFormWithComma();
			if (concatFormWithComma.length() != 0 && concatFormWithComma.length() < 32766) { // Temporary solution to fix: "Document contains at least one immense term in field="AFFILIATION_UNTOKENIZED"
				document.add(new Field(DocumentVectorType.AFFILIATION_UNTOKENIZED.name(), concatFormWithComma, untokenizedFieldType));
			}
		}

		// Add Journal Issue PubDate year.
		if (reCiterArticle.getJournal() != null) {
			document.add(new Field(DocumentVectorType.JOURNAL_ISSUE_PUBDATE_YEAR.name(), 
					Integer.toString(reCiterArticle.getJournal().getJournalIssuePubDateYear()),
					untokenizedFieldType));
		}
		
		// Add Journal ISOAbbreviation.
		if (reCiterArticle.getJournal() != null) {
			document.add(new Field(DocumentVectorType.JOURNAL_ISO_ABBRV.name(),
					reCiterArticle.getJournal().getIsoAbbreviation(),
					untokenizedFieldType));
		}
		
		// Add Scopus Affiliation.
		if (reCiterArticle.getScopusAffiliation() != null) {
			document.add(new Field(DocumentVectorType.SCOPUS_AFFILIATION_UNTOKENIZED.name(),
					reCiterArticle.getScopusAffiliation(),
					untokenizedFieldType));
		}
		
		return document;
	}
}

//fieldType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

//ft.setTokenized(false);
//ft.setStored(true);
//ft.setIndexed(true);

//FieldType ftNA = new FieldType(StringField.TYPE_STORED);
//ftNA.setTokenized(true);
//ftNA.setStored(true);
//StringField stringField = new StringField(DocumentVectorType.ARTICLE_TITLE.name(), reCiterArticle.getArticleTitle().getTitle(), Field.Store.YES);
//document.add(stringField);
