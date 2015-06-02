# ReCiter

## ReCiter wiki
The <a href="../../wiki">wiki</a> includes descriptions of files used for computation, an overview of error analysis, a log of performance, and use cases, among other informational material on the project.

## Setting up development environment
1. Install `Java JDK 8` from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.
2. Download `Eclipse Luna` from http://eclipse.org/downloads/.
3. Follow the instructions here (http://www.vogella.com/tutorials/EclipseGit/article.html) to install `EGit` for `Eclipse`.
4. Install `Maven Integration for Eclipse` in Eclipse Marketplace.
5. Do `git clone` a copy of ReCiter in an empty directory.
6. Open `Eclipse`, go to `File` -> `New` -> `Java Project` -> Enter `Project name` -> Uncheck `Use default location` -> `Browse` to the location of the cloned ReCiter project -> `Finish`.
7. Right click project -> If you have installed `Maven Integration for Eclipse` successfully in Step 4, you should be able to see `Maven` -> select `Update Project`.
8. Right click Project -> `Build Path` -> `Add Libraries` -> JUnit -> Select `JUnit 4` -> `Finish`.
9. Replace `data` folder with the unzipped `data.7z`.

## MySQL Setup
1. If you do not have MySQL installed, download it from https://www.mysql.com and install. (Any current version of MySQL will work) 
2. To connect to your local development environment's database:
3. Change the following in *database.properties*.
```
	url = "jdbc:mysql://localhost/reciter";
	username = "root";
	password = "";
```
4. Download the ReCiter database .SQL file (See the <a href="../../wiki">wiki</a> for information on how to obtain this file and additional files that may optionally be used with ReCiter)
5. Use your preferred database management tool to import the .SQL file to your localhost database. If using MySQL workbench, select "Data Import/Restore" in the left navigation bar; in the "Import from Disk" tab, select "Import from Self-Contained File"; and select "Start Import".

## XML data setup
1. Download the xml file *xml.7z*. (See the <a href="../../wiki">wiki</a> for information on how to obtain this file)
2. In Eclipse, create a folder named *data* with four inner folders *lucene_index*, *scopus*, *csv_output*, and *properties*.
3. Unzip *xml.7z* into *data*.

## Running ReCiter
### Running ReCiter for a specific person:

1. Edit *config.properties*
2. Run *src/test/examples/pubmed/ReCiterExample.java*

### Running ReCiter for all cwids
To run ReCiter for all cwids:

1. Generate all the configuration files for cwids by running */src/test/examples/pubmed/ConfigWriterTest.java*
2. Run *src/test/examples/pubmed/ReCiterExample.java*
3. The precision and recall values for each cwid are written to *reciter.log*

### Running ReCiter for a collection of specific cwids
To run ReCiter for a collection of cwids, first obtain XML files for the target authors. Then:

1. If desired, empty the contents of ReCiter/data/csv_output
2. Verify that there is an empty folder lucene_index within ReCiter/data/ (If it is not there, create it)
3. Verify that there is an empty folder properties within ReCiter/data/ (If it is not there, create it)
4. Verify that there is an empty folder xml within ReCiter/data/ (If it is not there, create it)
5. Copy XML files for the target authors into ReCiter/data/test_xml/
6. Run *src/test/examples/pubmed/ConfigWriterTest.java*, which outputs configurations for all target authors to the  ReCiter/data/properties folder
7. In *src/test/examples/pubmed/ReCiterExampleTest.java*, line 80 reads as follows:
```
	reCiterConfigProperty.setPerformRetrievePublication(true);
```
Setting the value to false assumes that xml files for the 64 cwids are already retrieved. Therefore, this value should be set to false after they have been retrieved. Verify that this line is not commented out.
8. Run *src/test/examples/pubmed/ReCiterExampleTest.java* which will fetch an updated copy of the XML files and run the clustering for all the 64 cwids.

Jie's comments to Michael about the above, from 5-16-15:
Running ReCiterExampleTest.java will fetch the updated xml files for the 64 cwids provided that the retrieve boolean variable is set to true. You can set it to false the second time. Running ReCiterExampleTest.java will also run the clustering for the 64 cwids. Also make sure that all the config files are fetched from database by running ConfigWriterTest.java which will populate the information such as cwid, first name, last name, primary department, etc. that are used for Phase 2 matching.

## Examining ReCiter output
ReCiter writes output to csv_output.csv in `data/csv_output`.
The precision and recall can be found in `reciter.log` after running `ReCiterExample.java`.

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
| Class | Lines of code | Description | Completeness |
|--------------------------|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| AbstractCosineSimilarity | 16 | Abstract class that implements DocumentSimilarity; calculates cosine similarity between two DocumentVectors. | Completed. Requires Testing. |
| ArticleCompleteness | 8 | Computes completeness with respect to ReCiterArticle article and ReCiterAuthor target. | Completed. |
| AuthorAffiliation | 18 | Getter and setter for author affiliation. | Completed. |
| AuthorName | 289 | Completed. Requires Testing. |  |
| Clusterer | 21 | Constructs a String from last name and first initial into a format that can be used to search the name in PubMed; matches two names accounting for variants; performs partial match on part of a name; returns name variants. |  |
| CosineSimilarity | 30 | Calculates cosine similarity between two DocumentVectors. | Completed. Requires Testing. |
| DocumentIndexReader | 143 | Checks whether cwid has been indexed before; creates directory if the directory cwid doesn't exist; opens the indexer; initializes all the document terms (for Sparse vector index); reads the indexed files into a ReCiterDocument; gets PMID of current article; creates a new ReCiterDocument; adds the DocumentVector (sparse vectors containing feature data) to current ReCiterDocument; reads the co-authors; creates new co-authors; adds them to list. | Requires Testing. Need to add logging. |
| DocumentIndexWriter | 134 | Writes Lucene indexes. | Requires Testing. Need to add a boolean to choose custom stemming or default Lucene stemming. |
| DocumentTerm | 189 | All document terms for a feature. Includes word to document map of which documents have particular words. Initializes allAuthorTerms by reading from index. | Completed. Requires Testing. |
| DocumentTranslator | 85 | Converts a ReCiterArticle to a Lucene Document; returns a Lucene Document which contains Fields corresponding to the fields of the ReCiterArticle object. Fields in Lucene Document are PMID, author, article title, journal title, keywords, and affiliations. | Completed. Requires Testing. |
| DocumentVector | 88 | Includes PMID of the ReCiterArticle, term to frequency map, type of vector, sparse vector containing frequency of the term, and DocumentVectorSimilarity similarity measure. | Completed. |
| DocumentVectorGenerator | 89 | Creates a DocumentVector for a specific field of a ReCiterArticle; creates a document array of a specific ReCiterArticle. | Need to add an option to allow for an user to select between TF-IDF weighting and unweighted terms. |
| DocumentVectorSimilarity | 12 | Calculates similarity between two document vectors. | Completed. |
| MaxCosineSimilarity | 33 | To be updated | Completed. Requires Testing. |
| ReCiterArticle | 123 | Defines ReCiterArticle object; articleID, articleTitle, articleCoAuthors, journal, articleKeywords, completenessScore, articleCompleteness, documentVectors, documentSimilarity | Completed. |
| ReCiterArticleCoAuthors | 49 | Defines ReCiterArticleCoAuthors object, an ArrayList of ReCiterAuthor objects | Completed. |
| ReCiterArticleKeywords | 51 | Provides methods for handling ReCiter keywords | Completed. |
| ReCiterAuthor | 26 | Defines ReCiterAuthor object consisting of AuthorName and AuthorAffiliation | Completed. |
| ReCiterCluster | 156 | Calculates the similarity of two clusters; calculates the similarity between a ReCiterArticle and a ReCiterCluster; checks whether the current cluster contains an author who has a name variant of that of the target author; gets the number of matching co-authors (excluding the target author) for each of the articles in the current cluster; returns the maximum number of matching co-authors among the articles in the current cluster. | Requires extensive testing. |
| ReCiterClusterer | 315 | Initializes finalCluster map data structure; performs hierarchical agglomerative clustering; identifies candidate clusters; if groups have matching co-authors, selects the group that has the most matching names; if two or more have the same number of co-authors, selects the one with the highest matching score; if groups have no matching co-authors, selects the group with the highest match based on cosine similarity, provided that the score exceeds a given threshold; computes co-author matches of current article with all current clusters. | Requires extensive testing. |
| ReCiterCompleteness | 36 | Implements ArticleCompleteness; calculates completeness given a ReCiterArticle and ReCiterAuthor | Completed. Requires Testing. |
| ReCiterJournal | 20 | ReCiterArticle journal field | Completed. |
| ReCiterArticleTitle | 21 | ReCiterArticle title field. | Completed. |
| WeightedCosineSimilarity | 55 | Computes cosine similarity between two documentVectors with respect to affiliation, article title, journal title, and keyword. | Completed. Requires Testing. |
