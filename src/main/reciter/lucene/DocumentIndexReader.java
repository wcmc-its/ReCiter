package main.reciter.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.article.ReCiterArticleCoAuthors;
import main.reciter.model.author.AuthorName;
import main.reciter.model.author.ReCiterAuthor;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentIndexReader {

	private IndexReader indexReader;
	private final static Logger slf4jLogger = LoggerFactory.getLogger(DocumentIndexReader.class);	
	
	/**
	 * Check whether this cwid has been indexed before.
	 * @param cwid cwid of the researcher.
	 * @return <code>true</code> if the cwid has been indexed, false otherwise.
	 */
	public boolean isIndexed(String cwid) {
		return new File(DocumentIndexWriter.DIR_PATH + cwid).isDirectory();
	}
	
	public List<ReCiterArticle> readIndex(String cwid) {
		slf4jLogger.info("Reading index ...");
		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
		try {
			
			// Create directory if the directory cwid doesn't exist.
			File newFile = new File(DocumentIndexWriter.DIR_PATH + cwid);
			if (!newFile.exists()) {
				slf4jLogger.info(DocumentIndexWriter.DIR_PATH + cwid + " directory doesn't exist.");
//				newFile.mkdir();
			}
			
			// Open the IndexReader.
			setIndexReader(DirectoryReader.open(FSDirectory.open(newFile)));
			
			// Initialize all the document terms (for Sparse vector index).
			DocumentTerm documentTerms = new DocumentTerm();
			documentTerms.initAllTerms(indexReader);
			
			DocumentVectorGenerator docVectorGenerator = new DocumentVectorGenerator();
			
			// Read the indexed files into a ReCiterDocument.
			for (int i = 0; i < indexReader.maxDoc(); i++) {
				
				// Get PMID of current article.
				Terms idVector = null;
				idVector = indexReader.getTermVector(i, DocumentVectorType.PMID.name());
				String pmid = null;
				TermsEnum termsEnum = null;
				if (idVector != null) {
					termsEnum = idVector.iterator(termsEnum);
					BytesRef text = null;
					while ((text = termsEnum.next()) != null) {
						pmid = text.utf8ToString(); // got PMID!
					}
				}
				
				// Create a new ReCiterDocument.
				ReCiterArticle reCiterArticle = new ReCiterArticle(Integer.parseInt(pmid)); // set PMID.

				DocumentVector[] docVectorArray;
				docVectorArray = docVectorGenerator.getDocumentVectors(indexReader, i, documentTerms);
				DocumentVector docV0 = docVectorArray[0];
				DocumentVector docV1 = docVectorArray[1];
				DocumentVector docV2 = docVectorArray[2];
				DocumentVector docV3 = docVectorArray[3];

				Map<DocumentVectorType, DocumentVector> map = new HashMap<DocumentVectorType, DocumentVector>();
				map.put(DocumentVectorType.AFFILIATION, docV0);
				map.put(DocumentVectorType.ARTICLE_TITLE, docV1);
				map.put(DocumentVectorType.JOURNAL_TITLE, docV2);
				map.put(DocumentVectorType.KEYWORD, docV3);
				
				// Add the DocumentVector (sparse vectors containing feature data) to this ReCiterDocument.
				reCiterArticle.setDocumentVectors(map);
				
				// Read the co-authors.
				Terms authorSizeTerms = null;
				authorSizeTerms = indexReader.getTermVector(i, DocumentVectorType.AUTHOR_SIZE.name());
				String authorSizeStr = null;
				TermsEnum authorSizeTermsEnum = null;
				if (authorSizeTerms != null) {
					authorSizeTermsEnum = authorSizeTerms.iterator(authorSizeTermsEnum);
					BytesRef text = null;
					while ((text = authorSizeTermsEnum.next()) != null) {
						authorSizeStr = text.utf8ToString(); // got PMID!
					}
				}
				int authorSize = Integer.parseInt(authorSizeStr);
				
				// create new coauthors
				reCiterArticle.setArticleCoAuthors(new ReCiterArticleCoAuthors());
				for (int j = 0; j < authorSize; j++) {
					Terms authorNameTerms = null;
					authorNameTerms = indexReader.getTermVector(i, DocumentVectorType.AUTHOR.name() + "_" + j);
					StringBuilder sb = new StringBuilder();
					TermsEnum authorNameTermsEnum = null;
					if (authorNameTerms != null) {
						authorNameTermsEnum = authorNameTerms.iterator(authorNameTermsEnum);
						BytesRef text = null;
						while ((text = authorNameTermsEnum.next()) != null) {
							sb.append(text.utf8ToString());
							sb.append(" ");
						}
					}
					reCiterArticle.getArticleCoAuthors().addCoAuthor(new ReCiterAuthor(AuthorName.deFormatLucene(sb.toString()), null));
				}
				
				// Add to list.
				reCiterArticleList.add(reCiterArticle);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		slf4jLogger.info("Finished reading index ...");
		return reCiterArticleList;
	}

	public IndexReader getIndexReader() {
		return indexReader;
	}

	public void setIndexReader(IndexReader indexReader) {
		this.indexReader = indexReader;
	}
}
