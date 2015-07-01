# ReCiter

## ReCiter wiki
The <a href="../../wiki">wiki</a> includes descriptions of files used for computation, an overview of error analysis, a log of performance, and use cases, among other informational material on the project.

## Setting up development environment
1. Install `Java JDK 8` from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html.
2. Download `Eclipse Luna` from http://eclipse.org/downloads/.
3. Follow the instructions here (http://www.vogella.com/tutorials/EclipseGit/article.html) to install `EGit` for `Eclipse`.
4. Install `Maven Integration for Eclipse` in Eclipse Marketplace (https://marketplace.eclipse.org/content/maven-integration-eclipse-luna).
5.Use the `git clone` command to clone a copy of ReCiter in an empty directory: Using the command line, navigate to the directory in which you wish to install ReCiter, then type git clone https://github.com/wcmc-its/ReCiter.git
6. Open `Eclipse`, go to `File` -> `New` -> `Java Project` -> Enter `Project name` -> Uncheck `Use default location` -> `Browse` to the location of the cloned ReCiter project -> Identify the parent folder of the cloned ReCiter project, `ReCiter` -> `Finish`.
7. Unzip `data.7z` and place the resulting `data` folder in the project's `ReCiter` directory
8. Right-click the project in Eclipse and select `refresh`
9. Download `JDBC Driver for MySQL (Connector/J)` from https://www.mysql.com/products/connector/
10. Right-click the project in Eclipse, select `properties`, `java build path`, `Add external Jars...` and navigate to the JDBC Driver's .jar file (the file will be named similarly to `mysql-connector-java-5.1.35-bin.jar`). Once you select this .jar file, it will show up under `referenced libraries`.

## MySQL Setup
1. If you do not have MySQL installed, download it from https://www.mysql.com and install. (Any current version of MySQL will work) 
2. To connect to your local development environment's database, change `/src/main/resources/config/database.properties` to your own local MySQL login information.
Example 1:
```
	url = "jdbc:mysql://localhost/reciter";
	username = "root";
	password = "";
```
Example 2:
```
	url = "jdbc:mysql://localhost/reciter";
	username = "reciter_pubs";
	password = "your_password_goes_here";
```
Download the ReCiter database .SQL file (See the <a href="../../wiki">wiki</a> for information on how to obtain this file and additional files that may optionally be used with ReCiter)
Use your preferred database management tool to import the .SQL file to your localhost database. If using MySQL workbench, select `Data Import/Restore` in the left navigation bar; in the `Import from Disk` tab, select `Import from Self-Contained File` and select "Start Import`. To import the .SQL file using the command line, follow these steps:
Open a terminal window or command line
Navigate to the `bin` directory in `/usr/local/mysql`:
`cd /usr/local/mysql/bin`
`mysql -u root -p`
When prompted, enter your password for your local mysql server
Once at the MYSQL prompt, create the reciter database:
`CREATE DATABASE IF NOT EXISTS reciter;`
`exit`

From the command line, use this command to import the reciter database from the SQL file:
`mysql -u root -p reciter < "ReCiterDB.sql"`
When prompted, enter your password for your local mysql server.
Wait for the process to complete (it may take a few minutes).

## Running ReCiter
Verify that your local SQL server is running. If it isn't, start it.
For example, open a terminal and run these commands:
cd /usr/local/mysql/support-files
./mysql.server start

### For a single cwid:
In `Eclipse`, run `/src/test/java/reciter/algorithm/cluster/ReCiterExampleSingleCwid.java`.

### For the 63 cwids in the reference standard:
In `Eclipse`, run `/src/test/java/reciter/algorithm/cluster/ReCiterExample.java`.

## Committing code from Eclipse to GitHub.
1. Inside Eclipse, Right Click project -> Select `Team` -> Select `Commit` -> Select `Push to Upstream`.

## Examining ReCiter output.
1. ReCiter outputs a csv file for each cwid in the folder `data/csv_output/`.
2. The precision and recall can be found in `reciter.log` or inside `Eclipse` console.

## Special Note for Eclipse UTF-8 Encoding Configuration

For Eclipse IDE development setup, use default encoding format as UTF-8 to avoid the special characters encoding from PubMed XMl files.  <br></br>
This note address the issue rised in GitHub for issue Manage special characters in PubMed data #87  <br></br>
https://github.com/wcmc-its/ReCiter/issues/87 <br></br>
1. Go to Eclipse --> Right Click on ReCiter --> Properties --> Resources  <br></br>
2.  Under Text file encoding -->  Chenage from Inherited from container ( Cp1252 ) to Other UTF-8 from selection list option <br></br>




