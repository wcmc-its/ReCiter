# ReCiter

## ReCiter wiki
The <a href="../../wiki">wiki</a> includes descriptions of files used for computation, an overview of error analysis, a log of performance, and use cases, among other informational material on the project.

## Setting up development environment
1. Install `Java JDK 8` from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.
2. Download the latest version of Eclipse from http://eclipse.org/downloads/. As of 9-24-15, the latest version was `Eclipse Mars`.
3. Follow the instructions here (http://www.vogella.com/tutorials/EclipseGit/article.html) to install `EGit` for `Eclipse`.
4. Install `Maven Integration for Eclipse` in Eclipse Marketplace (https://marketplace.eclipse.org/content/maven-integration-eclipse-luna). (As of 1-12-16, `Maven Integration for Eclipse Luna` was the most recent version available for Eclipse Mars.) 
5. Use the `git clone` command to clone a copy of ReCiter in an empty directory: Using the command line, navigate to the directory in which you wish to install ReCiter, then type `git clone https://github.com/wcmc-its/ReCiter.git`.
6. Open `Eclipse`, go to `File` -> `New` -> `Java Project` -> Enter `Project name` -> Uncheck `Use default location` -> `Browse` to the location of the cloned ReCiter project -> Identify the parent folder of the cloned ReCiter project, `ReCiter` -> `Finish`.
7. Unzip `data.7z` and place the resulting `data` folder in the project's `/src/main/resources/` directory (Note that .7z is a type of compressed file that can only be unzipped by software capable of reading files of this type, such as <a href="http://www.7-zip.org">7-zip</a>).
8. Right-click the project in Eclipse and select `refresh`.
9. Download `JDBC Driver for MySQL (Connector/J)` from https://www.mysql.com/products/connector/ and make a note of the location to which you have downloaded, for use in the next step.
10. Right-click the project in Eclipse, select `properties`, `java build path`, `Add external Jars...` and navigate to the JDBC Driver's .jar file (the file will be named similarly to `mysql-connector-java-5.1.35-bin.jar`). Once you select this .jar file, it will show up under `referenced libraries`.

## MySQL Setup
1. If you do not have MySQL installed, download it from https://www.mysql.com and install. (Any current version of MySQL will work) 
2. To connect to your local development environment's database, change `/src/main/resources/config/database.properties` to your own local MySQL login information (see Examples 1 to 3, below). To connect to the WCMC ReCiter database, see example 4, below.

	Example 1:
	```
	url=jdbc:mysql://localhost/reciter
	username=root
	password=
	```
	Example 2:
	```
	url=jdbc:mysql://localhost/reciter
	username=root
	password=your_password_goes_here
	```
	Example 3:
	```
	url=jdbc\:mysql\://localhost/reciter
	username=root
	password=your_password_goes_here
	```
	Example 4:
	```
	url=jdbc:mysql://its-y ... 01.med.cornell.edu/reciter (full database path is excluded)
	username=reciter_pubs
	password=database_password_goes_here
	```
3. If you wish to run ReCiter locally rather than from the ReCiter database on the WCMC server, download the ReCiter database .SQL file (See the <a href="../../wiki">wiki</a> for information on how to obtain this file and additional files that may optionally be used with ReCiter).
4. Use your preferred database management tool to import the .SQL file to your localhost database. If using MySQL workbench, select `Data Import/Restore` in the left navigation bar; in the `Import from Disk` tab, select `Import from Self-Contained File` and select `Start Import`. To import the .SQL file using the command line, follow these steps:

1. Open a terminal window or command line
2. Navigate to the `bin` directory in `/usr/local/mysql`:
	`cd /usr/local/mysql/bin`
	`mysql -u root -p`
3. When prompted, enter your password for your local mysql server
4. Once at the MYSQL prompt, create the reciter database:
	`CREATE DATABASE IF NOT EXISTS reciter;`
	`exit`
5. From the command line, use this command to import the reciter database from the SQL file:
	`mysql -u root -p reciter < "ReCiterDB.sql"`
6. When prompted, enter your password for your local mysql server.
7. Wait for the process to complete (it may take a few minutes).

## Running ReCiter

If you are running an instance of ReCiter that will be connecting to the ReCiter database, it is necessary to verify that there is a row for the target author in the rc_identity table. If not, it's necessary to add a row for the person. When connecting to the database ReCiter won't run for a target author without a row in rc_identity.

### For a single cwid
Note: if you are connecting to the ReCiter database, there must be a row for the target author in the rc_identity table.
1. In `Eclipse`, edit `/src/test/java/reciter/algorithm/cluster/ReCiterExampleRunSingle.java`.
2. Go to the line after `ReCiterExampleRunSingle example = new ReCiterExampleRunSingle();`
3. Edit the line that looks like this, replacing the cwid with that of your target author `example.engine.run("als7001");`
4. Right-click the class in the left navigation pane, select `Run As`, then `Java Application`

### For the 63 cwids in the reference standard:
Note: there must be a row for each target author in the rc_identity table.
1. In `Eclipse`, edit `/src/test/java/reciter/algorithm/cluster/ReCiterExample.java`.
2. If needed, add two forward-slashes to comment out the line that runs ReCiter for a single CWID
3. Ensure that the following line is not commented out: `runTestCwids();`
4. Right-click the class in the left navigation pane, select `Run As`, then `Java Application`.

## Committing code from Eclipse to GitHub.
1. Inside Eclipse, Right Click project -> Select `Team` -> Select `Commit` -> Select `Push to Upstream`.

## Retrieving the latest code commits from GitHub.
1. Inside Eclipse, Right Click project -> Select `Team` -> Select `Fetch from Upstream`. This will fetch the latest version of code from GitHub into your local repository.
2. Right Click project -> Select `Team` -> Select `Pull`. This will pull the latest version of code from your local repository into the Eclipse project.

## Examining ReCiter output.
1. ReCiter outputs a csv file for each cwid in the folder `ReCiter/src/main/resources/data/csv_output/`.
2. The precision and recall can be found in `reciter.log` or inside the `Eclipse` console.

## Special Note for Eclipse UTF-8 Encoding Configuration

For Eclipse IDE development setup, use default encoding format as UTF-8 to avoid the special characters encoding from PubMed XML files.  <br>
This note address the issue raised in GitHub for issue Manage special characters in PubMed data #87  <br>
https://github.com/wcmc-its/ReCiter/issues/87 <br>
1. Go to Eclipse --> Right Click on ReCiter --> Properties --> Resources  <br>
2. Under Text file encoding -->  Change from Inherited from container ( Cp1252 ) to Other UTF-8 from selection list option <br>

## Troubleshooting

If you encounter errors while running ReCiter:<br>
1. Verify that your Internet connection is operational<br>
2. Verify that your local SQL server is running. If it isn't, start it. For example, open a terminal and run these commands:<br>
	```
	cd /usr/local/mysql/support-files
	```
	<br>
	```
	./mysql.server start
	```
<br>
3. Open a MYSQL prompt and type the following command, replacing PASSWORD with the root password for your local MYSQL instance: grant all privileges on *.* to 'root'@'localhost' identified by 'PASSWORD' with grant option;<br>
4. In config.properties, make sure that the username and password are not enclosed in quotation marks; likewise, ensure that lines do not end with a semicolon (neither the quotation marks nor the semicolon are needed in the configuration file, and they may cause errors if present)<br>
5. Double-check that you have entered the correct password for 'root' in config.properties<br>

### Known Eclipse bug 67414

If when compiling ReCiter you encounter this message:

```
The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files
```
To resolve this issue, follow the instructions at http://bit.ly/1PyyBNO or at http://dev-answers.blogspot.de/2009/06/eclipse-build-errors-javalangobject.html

### Missing data for a CWID

If when running ReCiter you encounter output that looks like this:
```
 INFO [main] (ReCiterExample.java:67) - Number of cwids: 0
 INFO [main] (ReCiterExample.java:68) - Average Precision: NaN
 INFO [main] (ReCiterExample.java:69) - Average Recall: NaN
 INFO [main] (ReCiterExample.java:75) - Total execution time: 92 ms.
 ```
Then, double-check that you have a folder for the CWID or CWIDs that you're running in resources/data/pubmed

### Local SQL server not running

If you encounter this error:
```
 INFO [main] (ReCiterExample.java:162) - finished getting Scopus Xml
 INFO [main] (DocumentIndexReader.java:43) - Reading Lucene index for aas2004
 INFO [main] (DocumentIndexReader.java:251) - Finished Reading Lucene index for aas2004
com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
```
Then, verify that your local SQL server is running (see point 2 under "Troubleshooting" above).

### .DS_Store files interfere with ReCiter

If you encounter this error:
```
src/main/resources/data/properties/.DS_Store/.DS_Store.properties
java.io.FileNotFoundException: src/main/resources/data/properties/.DS_Store/.DS_Store.properties (Not a directory)
```
Then, follow these steps:

1. Delete any .DS_Store files in your ReCiter installation directory and its subfolders (to delete recursively, go to parent folder and use this command:<br>
```
find . -name '*.DS_Store' -type f -delete
```
<br>
2. Install and run DeathToDSStore, available for download at https://www.aorensoftware.com/blog/2011/12/24/death-to-ds_store/<br>
3. (Optional step) Verify that there are no longer any .DS_Store files in your ReCiter installation directory and its subfolders<br>
4. In Eclipse, right-click the project and click refresh<br>
5. Try running again

### ReCiterExample.java not getting input data from the correct location

If you encounter this error:
```
Exception in thread "main" java.nio.file.NoSuchFileException: src/main/resources/data/pubmed
```
Then, follow these steps:

1. Navigate to /ReCiter/src/main/resources/data
2. Rename the folder "pubmed" to "pubmed1"
3. Rename the folder "xml" to "pubmed"
4. In Eclipse, right-click the project and click refresh<br>
5. Try running again

### Permissions error in Eclipse

If when attempting to run ReCiter using a Mac you encounter an error like this:
```
An error occurred while creating the Java project. Reason: Parent of resource: / ... / ReCiter/target is marked as read-only
```
1. Go to the folder mentioned in the error messages
2. Click on `get info`
3. Select the lock to unlock privileges
4. Change the privileges to `read and write`.
5. Try running again

### Problem with MySQL setup in database.properties

If when attempting to run ReCiter you encounter an error like this:
```
java.sql.SQLException: Access denied for user 'root'@'localhost' (using password: NO)
```
1. Open the database properties file at /src/main/resources/config/database.properties
2. Follow the instructions under "MySQL Setup" above to connect ReCiter to the ReCiter database 
3. Try running again

### Value for max_allowed_packet is too small

If when attempting to run ReCiter you encounter an error like this:
```
java.sql.BatchUpdateException: Packet for query is too large (19926700 > 1048576). You can change this value on the server by setting the max_allowed_packet' variable.
```
1. Execute this query on your MYSQL server: `SET GLOBAL max_allowed_packet=1073741824;`
2. Try running again. (Note that the value for max_allowed_packet will reset when your MYSQL server restarts.)

### Author name is not in the rc_identity data table of the ReCiter database

If when attempting to run ReCiter you encounter an error like this:
```
Exception in thread "main" java.lang.StringIndexOutOfBoundsException: String index out of range: 1
	at java.lang.String.substring(String.java:1950)
	at reciter.service.impl.TargetAuthorServiceImpl.getPubMedSearchQuery(TargetAuthorServiceImpl.java:128)
	at reciter.service.impl.TargetAuthorServiceImpl.getTargetAuthor(TargetAuthorServiceImpl.java:120)
	at reciter.engine.ReCiterEngine.run(ReCiterEngine.java:123)
	at reciter.algorithm.cluster.ReCiterExample.main(ReCiterExample.java:26)
```
Verify that there is a row in rc_identity that contains the CWID of the target author.

| Phase                        | Class                          | Method                            | Purpose                                                                                                                                                                                                    |
|------------------------------|--------------------------------|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Retrieval                    | ReCiterExample                 | main                              | Invoke ReCiter                                                                                                                                                                                             |
| Retrieval                    | ReCiterEngine                  | run                               | Empty analysis table                                                                                                                                                                                       |
| Retrieval                    | ReCiterEngine                  | run                               | Invoke retrieval of data on target author                                                                                                                                                                  |
| Retrieval                    | TargetAuthorServiceImpl        | getTargetAuthor                   | Set target author first name, middle name, last name, affiliation, CWID, known co-investigators, primary department, other department (if any), citizenship, educational degrees, and board certifications |
| Retrieval                    | TargetAuthorServiceImpl        | getTargetAuthor                   | Look up target author name aliases                                                                                                                                                                         |
| Retrieval                    | TargetAuthorServiceImpl        | getTargetAuthor                   | Include target author name aliases as part of input query                                                                                                                                                  |
| Retrieval                    | TargetAuthorServiceImpl        | getTargetAuthor                   | Set alternate department names for target author                                                                                                                                                           |
| Retrieval                    | TargetAuthorServiceImpl        | getTargetAuthor                   | Set target author e-mail and other e-mail                                                                                                                                                                  |
| Retrieval                    | ReCiterEngine                  | run                               | Get CWID of target author                                                                                                                                                                                  |
| Retrieval                    | ReCiterEngine                  | run                               | Invoke code to fetch articles for target author                                                                                                                                                            |
| Retrieval                    | ReCiterEngine                  | run                               | Read in reciter.properties file, which specifies locations of files on the local machine                                                                                                                   |
| Retrieval                    | ReCiterArticleFetcher          | fetchUsingLastNameFirstInitial    | Retrieve PubMed search query for target author                                                                                                                                                             |
| Retrieval                    | ReCiterArticleFetcher          | fetchUsingLastNameFirstInitial    | If they have not yet been retrieved, retrieve articles from PubMed for target author                                                                                                                       |
| Retrieval                    | ReCiterArticleFetcher          | fetchUsingLastNameFirstInitial    | If they have not yet been retrieved, retrieve articles from Scopus for target author                                                                                                                       |
| Retrieval                    | ReCiterEngine                  | run                               | Assign status with respect to gold standard to each article                                                                                                                                                |
| Phase 1 clustering           | ReCiterEngine                  | run                               | Invoke ReCiterClusterer which handles phase 1 clustering                                                                                                                                                   |
| Phase 1 clustering           | ReCiterClusterer               | ReCiterClusterer                  | Invoke code to cluster based on name matching                                                                                                                                                              |
| Phase 1 clustering           | ReCiterClusterer               | cluster                           | Do clustering based on name matching                                                                                                                                                                       |
| Phase 1 clustering           | NameMatchingClusteringStrategy | cluster                           | Create first cluster                                                                                                                                                                                       |
| Phase 1 clustering           | NameMatchingClusteringStrategy | cluster                           | Assign subsequent articles to clusters                                                                                                                                                                     |
| Phase 1 clustering           | NameMatchingClusteringStrategy | cluster                           | Measure similarity value between articles in cluster and article under evaluation                                                                                                                          |
| Phase 1 clustering           | NameMatchingClusteringStrategy | isTargetAuthorNameAndJournalMatch | Invoke code to evaluate whether the article under consideration matches one or more articles in the cluster based on first name, middle initial, and  journal                                              |
| Phase 1 clustering           | NameMatchingClusteringStrategy | isTargetAuthorNameAndJournalMatch | Check whether there is a first name match as well as a middle initial match between author in the article under consideration and names of authors in the cluster.                                         |
| Phase 1 clustering           | NameMatchingClusteringStrategy | isTargetAuthorNameAndJournalMatch | Check whether there is a match between the journal of the article under consideration and the journals in the cluster                                                                                      |
| Phase 1 clustering           | NameMatchingClusteringStrategy | cluster                           | If article is similar based on target author name and journal match, add it to the cluster                                                                                                                 |
| Phase 2 cluster selection    | ReCiterEngine                  | run                               | Perform phase two cluster selection                                                                                                                                                                        |
| Phase 2 cluster selection    | ReCiterClusterer               | getClusters                       | Get the clusters of articles                                                                                                                                                                               |
| Phase 2 cluster selection    | ReCiterClusterSelector         | selectClusters                    | Invoke code to select clusters that are similar to the target author, based on evidence types                                                                                                              |
| Phase 2 cluster selection    | ReCiterClusterSelector         | selectClusters                    | Invoke e-mail matching strategy                                                                                                                                                                            |
| Phase 2 cluster selection    | EmailStringMatchStrategy       | executeStrategy                   | Run e-mail matching strategy                                                                                                                                                                               |
| Phase 2 cluster selection    | EmailStringMatchStrategy       | executeStrategy                   | Increase e-mail match score if the e-mail address matches for any co-author                                                                                                                                |
| Phase 2 cluster selection    | EmailStringMatchStrategy       | executeStrategy                   | Return e-mail matching score                                                                                                                                                                               |
| Phase 2 cluster selection    | ReCiterClusterSelector         | selectClusters                    | Invoke department string matching strategy                                                                                                                                                                 |
| Phase 2 cluster selection    | DeapartmentStrategyContext     | executeStrategy                   | Run department string matching strategy                                                                                                                                                                    |
| Phase 2 cluster selection    | DepartmentStringMatchStrategy  | departmentMatchStrict             | Invoke strict department string matching calculation                                                                                                                                                       |
| Phase 2 cluster selection    | DepartmentStringMatchStrategy  | executeStrategy                   | Determine whether there is a match on department                                                                                                                                                           |
| Phase 2 cluster selection    | DepartmentStringMatchStrategy  | executeStrategy                   | Determine whether there is a match on first initial                                                                                                                                                        |
| Phase 2 cluster selection    | DepartmentStringMatchStrategy  | executeStrategy                   | If department and first name initial match, set DepartmentStrategyScore to 1                                                                                                                               |
| Phase 2 cluster selection    | ReCiterClusterSelector         | runSelectionStrategy              | If no cluster ids are selected, select the cluster for which the first and middle names match                                                                                                              |
| Phase 3 article reassignment | ReCiterClusterSelector         | runSelectionStrategy              | Invoke code to reassign individual articles that have one or more specific features that match those of the target author                                                                                  |
| Phase 3 article reassignment | ReCiterClusterSelector         | reAssignArticles                  | Reassign individual articles that are similar to the target author based on a given instance of strategy context                                                                                           |
| Phase 3 article reassignment | ReCiterClusterSelector         | handleTargetAuthorStrategyContext | Map of cluster ids to ReCiterArticle objects; keeps track of the new cluster ids that these ReCiterArticle objects will be placed at the end of the loop                                                   |
| Phase 3 article reassignment | ReCiterClusterSelector         | handleTargetAuthorStrategyContext | Iterate through articles in final cluster; remove selected articles as identified in the previous code                                                                                                     |
| Phase 3 article reassignment | ReCiterClusterSelector         | handleTargetAuthorStrategyContext | When an article to be moved is identified, move it to its new cluster                                                                                                                                      |
| Phase 3 article reassignment | ReCiterEngine                  | run                               | Invoke analysis of results of disambiguation                                                                                                                                                               |
| Output                       | Analysis                       | performAnalysis                   | Run analysis                                                                                                                                                                                               |
| Output                       | ReCiterEngine                  | run                               | Output status of each article to standard output                                                                                                                                                           |
| Output                       | ReCiterEngine                  | run                               | Output article data, including ReCiter disambiguation results, to ReCiter database                                                                                                                         |
| Output                       | ReCiterEngine                  | run                               | Output precision, recall, and accuracy to standard output                                                                                                                                                  |
| Output                       | ReCiterExample                 | main                              | Output time elapsed to standard output                                                                                                                                                                     |
