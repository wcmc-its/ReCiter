# ReCiter

## Eclipse Setup
1. Create a folder in your local development directory and name it "ReCiter"
2. Download a zip version of ReCiter and unzip it to your local "ReCiter" folder
3. If you have Maven installed, open a terminal window, navigate to the ReCiter folder location, and type mvn install; (if not, you can convert to Maven via Eclipse in Step 6, below).
4. Create a Java Project in Eclipse named "ReCiter." Indicate the path to the "ReCiter" folder as the root directory for the code base.
5. If you did not do a Maven install from the command line in Step 3, right-click the "ReCiter" project in Eclipse, select "Configure", then select "Convert to Maven Project."
6. Right-click "ReCiter"
9. Select "Build Path"
10. Select "Add Library"
11. Add "JUnit" library

## MySQL Setup
1. To connect to the VIVO MySQL database.
2. Change the following in *main/database/DbConnectionFactory.java*.
```
	private static final String URL = "jdbc:mysql://localhost/reciter?rewriteBatchedStatements=true";
	private static final String USER = "root";
	private static final String PASSWORD = "";
```
To the following:
```
	private static final String URL = "jdbc:mysql://its-yrkmysqlt01.med.cornell.edu/reciter?rewriteBatchedStatements=true";
	private static final String USER = "reciter_pubs";
	private static final String PASSWORD = password;
```

## XML data setup.
1. Download the xml file *xml.7z*.
2. In Eclipse, create a folder named *data* with 2 inner folders *lucene_index*, *scopus*.
3. Unzip *xml.7z* into *data* as well.

## Running ReCiter
1. To run ReCiter for a specific person.
2. Edit *config.properties*.
3. Run *src/test/examples/pubmed/ReCiterExample.java*.

## Components of ReCiter
The code for ReCiter consists of four main parts: database storage utilities, PubMed retriever and parser, Scopus retriever and parser, and ReCiter algorithm.

## ReCiter constants
ReCiter constants are as follows
* cluster threshold similarity
* target person to cluster
* cluster threshold value
* hierarchical agglomerative clustering (HAC) value
* similarity vectors
  * affiliation
  * co-authors
  * journals
  * keywords

## ReCiter class descriptions and completeness
| Class                    | Size in lines of code, as of 3-26-15 | Description                                                                                                                                                                                       | Completeness                 |
|--------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------|
| ArticleCompleteness      | 8                                    | Computes completeness with respect to ReCiterArticle article and ReCiterAuthor target.                                                                                                            | Completed                    |
| DocumentVectorType       | 11                                   | package main.reciter.lucene;public enum DocumentVectorType { ARTICLE_TITLE, JOURNAL_TITLE, KEYWORD, AFFILIATION, PMID, AUTHOR, AUTHOR_SIZE,}                                                      | Completed.                   |
| DocumentVectorSimilarity | 12                                   | public interface DocumentVectorSimilarity { SparseRealVector normalize(SparseRealVector sparseRealVector); double similarity(DocumentVector vectorA, DocumentVector vectorB); String getType(); } | Completed.                   |
| AbstractCosineSimilarity | 16                                   | Abstract class that implements DocumentSimilarity; calculates cosine similarity between two DocumentVectors.                                                                                      | Completed. Requires testing. |
| AuthorAffiliation        | 18                                   | Getter and setter for author affiliation                                                                                                                                                          | Completed.                   |
| ReCiterJournal           | 20                                   | ReCiterArticle journal field                                                                                                                                                                      | Completed.                   |
| Clusterer                | 21                                   | Clusterer interface. Provides a set of functions that must be implemented by any clustering class that implements this interface. Performs a clustering on the list of ReCiterArticles.           | Completed.                   |
| ReCiterArticleTitle      | 21                                   | ReCiterArticle title field.                                                                                                                                                                       | Completed.                   |
| ReCiterAuthor            | 26                                   | Defines ReCiterAuthor object consisting of AuthorName and AuthorAffiliation                                                                                                                       | Completed                    |
| CosineSimilarity         | 30                                   | Calculates cosine similarity between two DocumentVectors.                                                                                                                                         | Completed. Requires testing. |
| MaxCosineSimilarity      | 33                                   | To be updated                                                                                                                                                                                     | Completed. Requires testing. |
| ReCiterCompleteness      | 36                                   | Implements ArticleCompleteness; calculates completeness given a ReCiterArticle and ReCiterAuthor                                                                                                  | Completed. Requires testing. |
| ReCiterArticleCoAuthors  | 49                                   | Defines ReCiterArticleCoAuthors object, an ArrayList of ReCiterAuthor objects                                                                                                                     | Completed.                   |
| ReCiterArticleKeywords   | 51                                   | Provides methods for handling ReCiter keywords                                                                                                                     | Completed.                   |                                                 




