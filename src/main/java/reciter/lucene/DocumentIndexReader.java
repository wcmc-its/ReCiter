package reciter.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleCoAuthors;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterArticleTitle;
import reciter.model.article.ReCiterJournal;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;

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
		//		return new File(DocumentIndexWriter.DIR_PATH + cwid).isDirectory();
		return false; // April 12 Update - Set to false so that Target Author can be indexed as well.
	}

	public List<ReCiterArticle> readIndex(String cwid) {
		slf4jLogger.info("Reading Lucene index for " + cwid);
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
			documentTerms.initIDFMap(indexReader, DocumentVectorType.AFFILIATION);

			DocumentVectorGenerator docVectorGenerator = new DocumentVectorGenerator();

			// Read the indexed files into ReCiterDocuments.
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

				// Get title of current article.
				Terms titleVector = null;
				titleVector = indexReader.getTermVector(i, DocumentVectorType.ARTICLE_TITLE_UNTOKENIZED.name());
				String title = null;
				TermsEnum titleTermsEnum = null;
				if (titleVector != null) {
					titleTermsEnum = titleVector.iterator(titleTermsEnum);
					BytesRef text = null;
					while ((text = titleTermsEnum.next()) != null) {
						title = text.utf8ToString();
					}
				}
				reCiterArticle.setArticleTitle(new ReCiterArticleTitle(title));

				// Get journal title of current article.
				Terms journalVector = null;
				journalVector = indexReader.getTermVector(i, DocumentVectorType.JOURNAL_TITLE_UNTOKENIZED.name());
				String journalTitle = null;
				TermsEnum journalTitleTermsEnum = null;
				if (journalVector != null) {
					journalTitleTermsEnum = journalVector.iterator(journalTitleTermsEnum);
					BytesRef text = null;
					while ((text = journalTitleTermsEnum.next()) != null) {
						journalTitle = text.utf8ToString();
					}
				}
				reCiterArticle.setJournal(new ReCiterJournal(journalTitle));

				// Get keywords of the current article.
				Terms keywordVector = null;
				keywordVector = indexReader.getTermVector(i, DocumentVectorType.KEYWORD_UNTOKENIZED.name());
				String keyword = null;
				TermsEnum keywordTermsEnum = null;
				if (keywordVector != null) {
					keywordTermsEnum = keywordVector.iterator(keywordTermsEnum);
					BytesRef text = null;
					while ((text = keywordTermsEnum.next()) != null) {
						keyword = text.utf8ToString();
					}
				}
				reCiterArticle.setArticleKeywords(new ReCiterArticleKeywords());

				if (keyword != null) {
					String[] keywordArray = keyword.split(",");
					for (String mesh : keywordArray) {
						reCiterArticle.getArticleKeywords().addKeyword(mesh);
					}
				}
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

				// Read the co-authors. Get the number of coauthors for this article.
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

				// Fetch the coauthors (includes parsing the author names).
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

				// Fetch affiliation.
				Terms affiliationTerms = null;
				affiliationTerms = indexReader.getTermVector(i, DocumentVectorType.AFFILIATION_UNTOKENIZED.name());
				String affiliation = null;
				TermsEnum affiliationTermsEnum = null;
				if (affiliationTerms != null) {
					affiliationTermsEnum = affiliationTerms.iterator(affiliationTermsEnum);
					BytesRef text = null;
					while ((text = affiliationTermsEnum.next()) != null) {
						affiliation = text.utf8ToString(); // affiliation.
					}
				}
				reCiterArticle.setAffiliationConcatenated(affiliation);

				// Fetch Journal Issue PubDate year.
				Terms journalIssuePubDateYearTerms = null;
				journalIssuePubDateYearTerms = indexReader.getTermVector(i, DocumentVectorType.JOURNAL_ISSUE_PUBDATE_YEAR.name());
				String journalIssuePubDateYearString = null;
				TermsEnum journalIssuePubDateYearTermsEnum = null;
				if (journalIssuePubDateYearTerms != null) {
					journalIssuePubDateYearTermsEnum = journalIssuePubDateYearTerms.iterator(journalIssuePubDateYearTermsEnum);
					BytesRef text = null;
					while ((text = journalIssuePubDateYearTermsEnum.next()) != null) {
						journalIssuePubDateYearString = text.utf8ToString();
					}
				}

				// Skipping target author:
				if (reCiterArticle.getArticleID() != -1) {
					reCiterArticle.getJournal().setJournalIssuePubDateYear(Integer.parseInt(journalIssuePubDateYearString));
				}

				// Fetch Journal ISO Abbreviation.
				Terms journalISOAbbrvTerms = null;
				journalISOAbbrvTerms = indexReader.getTermVector(i, DocumentVectorType.JOURNAL_ISO_ABBRV.name());
				String journalISOAbbrv = null;
				TermsEnum journalISOAbbrvTermsEnum = null;
				if (journalISOAbbrvTerms != null) {
					journalISOAbbrvTermsEnum = journalISOAbbrvTerms.iterator(journalISOAbbrvTermsEnum);
					BytesRef text = null;
					while ((text = journalISOAbbrvTermsEnum.next()) != null) {
						journalISOAbbrv = text.utf8ToString();
					}
				}

				// Skipping target author:
				if (reCiterArticle.getArticleID() != -1) {
					reCiterArticle.getJournal().setIsoAbbreviation(journalISOAbbrv);
				}

				if (reCiterArticle.getArticleID() != -1) {
					// Fetch Scopus Affiliation.
					Terms scopusAffiliationTerms = null;
					scopusAffiliationTerms = indexReader.getTermVector(i, DocumentVectorType.SCOPUS_AFFILIATION_UNTOKENIZED.name());
					String scopusAffiliation = null;
					TermsEnum scopusAffiliationTermsEnum = null;
					if (scopusAffiliationTerms != null) {
						scopusAffiliationTermsEnum = scopusAffiliationTerms.iterator(scopusAffiliationTermsEnum);
						BytesRef text = null;
						while ((text = scopusAffiliationTermsEnum.next()) != null) {
							scopusAffiliation = text.utf8ToString();
						}
					}
					reCiterArticle.setScopusAffiliation(scopusAffiliation);
				}
				
				// Add to list.
				reCiterArticleList.add(reCiterArticle);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reCiterArticleList;
	}

	public IndexReader getIndexReader() {
		return indexReader;
	}

	public void setIndexReader(IndexReader indexReader) {
		this.indexReader = indexReader;
	}
}
