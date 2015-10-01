# ReCiter

## ReCiter wiki
The <a href="../../wiki">wiki</a> includes descriptions of files used for computation, an overview of error analysis, a log of performance, and use cases, among other informational material on the project.

## Setting up development environment
1. Install `Java JDK 8` from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.
2. Download the latest version of Eclipse from http://eclipse.org/downloads/. As of 9-24-15, the latest version was `Eclipse Mars`.
3. Follow the instructions here (http://www.vogella.com/tutorials/EclipseGit/article.html) to install `EGit` for `Eclipse`.
4. Install `Maven Integration for Eclipse` in Eclipse Marketplace (https://marketplace.eclipse.org/content/maven-integration-eclipse-luna). (As of 9-24-15, `Maven Integration for Eclipse` was the most recent version available for Eclipse Mars.) 
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

### For a single cwid:
In `Eclipse`, run `/src/test/java/reciter/algorithm/cluster/ReCiterExampleSingleCwid.java`.

### For the 63 cwids in the reference standard:
In `Eclipse`, run `/src/test/java/reciter/algorithm/cluster/ReCiterExample.java`.

## Committing code from Eclipse to GitHub.
1. Inside Eclipse, Right Click project -> Select `Team` -> Select `Commit` -> Select `Push to Upstream`.

## Examining ReCiter output.
1. ReCiter outputs a csv file for each cwid in the folder `ReCiter/src/main/resources/data/csv_output/`.
2. The precision and recall can be found in `reciter.log` or inside `Eclipse` console.

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
