package main.reciter.lucene;

import java.util.ArrayList;
import java.util.List;

import main.reciter.model.article.ReCiterArticle;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo;

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
		
		// Declare FieldType for a Field object.
		FieldType fieldType = new FieldType();
		fieldType.setIndexed(true);
		fieldType.setStored(true);
		fieldType.setStoreTermVectors(true);
		fieldType.setTokenized(true);
		fieldType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		
		// Create a new Lucene Document.
		Document document = new Document();
		
		// Add PMID field.
		document.add(new Field(DocumentVectorType.PMID.name(), Integer.toString(reCiterArticle.getArticleID()), fieldType));
		
		// Add Author field.
		if (reCiterArticle.getArticleCoAuthors() != null) {
			int numAuthors = reCiterArticle.getArticleCoAuthors().getNumberCoAuthors();
			document.add(new Field(DocumentVectorType.AUTHOR_SIZE.name(), Integer.toString(numAuthors), fieldType));
			
			for (int i = 0; i < numAuthors; i++) {
				// Field names are incremented by "_i":
				String indexableName = reCiterArticle.getArticleCoAuthors().getCoAuthors().get(i).getAuthorName().getLuceneIndexableFormat();
				document.add(new Field(DocumentVectorType.AUTHOR.name() + "_" + i, indexableName, fieldType));
			}
		}
		
		// Add article title field.
		if (reCiterArticle.getArticleTitle() != null) {
			Field field = new Field(DocumentVectorType.ARTICLE_TITLE.name(), reCiterArticle.getArticleTitle().getTitle(), fieldType);
			document.add(field);
		}
		
		// Add journal title field.
		if (reCiterArticle.getJournal() != null) {
			Field field = new Field(DocumentVectorType.JOURNAL_TITLE.name(), reCiterArticle.getJournal().getJournalTitle(), fieldType);
			document.add(field);
		}
		
		// Add keywords field.
		if (reCiterArticle.getArticleKeywords() != null) {
			Field field = new Field(DocumentVectorType.KEYWORD.name(), reCiterArticle.getArticleKeywords().getConcatForm(), fieldType);
			document.add(field);
		}
		
		// Add affiliations field.
		if (reCiterArticle.getArticleCoAuthors() != null) {
			String concat = reCiterArticle.getArticleCoAuthors().getAffiliationConcatForm();
			if (concat.length() != 0) {
				Field field = new Field(DocumentVectorType.AFFILIATION.name(), reCiterArticle.getArticleCoAuthors().getAffiliationConcatForm(), fieldType);
				document.add(field);
			}
		}
		
		return document;
	}
}
