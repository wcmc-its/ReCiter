# ReCiter
![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiVWFoRUNZQzBqMkJpTGQ4MHNNbUJkclE4OTRVV0Y0SzJBVTBVMmV5NVhyNUdpbXVDblkrcWZkWU9SQWFxV0dUand4d05iVFFlT0Z0bWJsamM1SnRaUFdFPSIsIml2UGFyYW1ldGVyU3BlYyI6ImkzMndTalROWWlXUC8yRW4iLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)
[![GitHub version](https://badge.fury.io/gh/wcmc-its%2FReCiter.svg)](https://badge.fury.io/gh/wcmc-its%2FReCiter)
[![codebeat badge](https://codebeat.co/badges/9845c96b-ed87-4b1a-b62c-1e0e8b51bbb8)](https://codebeat.co/projects/github-com-wcmc-its-reciter-master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Pending Pull-Requests](http://githubbadges.herokuapp.com/wcmc-its/ReCiter/pulls.svg?style=flat)](https://github.com/wcmc-its/ReCiter/pulls)
[![Open Issues](http://githubbadges.herokuapp.com/wcmc-its/ReCiter/issues.svg?style=flat)](https://github.com/wcmc-its/ReCiter/issues)
[![star this repo](http://githubbadges.com/star.svg?user=wcmc-its&repo=ReCiter&style=flat)](https://github.com/wcmc-its/ReCiter)
[![fork this repo](http://githubbadges.com/fork.svg?user=wcmc-its&repo=ReCiter&style=flat)](https://github.com/wcmc-its/ReCiter/fork)
[![Github All Releases](https://img.shields.io/github/downloads/wcmc-its/ReCiter/total.svg)]()
[![Open Source Love](https://badges.frapsoft.com/os/v3/open-source.svg?v=102)](https://github.com/wcmc-its/ReCiter/) 


## Summary

ReCiter is a system for making highly accurate guesses about author identity in publication metadata. ReCiter includes a Java application, a DynamoDB-hosted database, and a set of RESTful microservices which collectively allow institutions to maintain accurate and up-to-date author publication lists for thousands of people. This software is optimized for disambiguating authorship in PubMed and, optionally, Scopus.

ReCiter rapidly and accurately identifies articles, including those at previous affiliations, by a given person. It does this by leveraging institutionally maintained identity data (e.g., departments, relationships, email addresses, year of degree, etc.) With the more complete and efficient searches that result from combining these types of data, you can save time and your institution can be more productive. If you run ReCiter daily, you can ensure that the desired users are the first to learn when a new publication has hit PubMed.

ReCiter is freely available and open source.  



## Technical 

### Prerequisites
- Java 1.8
- Latest version of Maven

It is not necessary to install ReCiter in order to use the API.

### Technological stack
Key technologies include:
- ReCiter stores data about researchers and publications in **DynamoDB**, which can be hosted on Amazon AWS on installed locally.
- Its main computation logic is written in **Java**.
- It employs the **Spring Framework**, a Java-based application framework designed to manage RESTful web services and server requests.
- ReCiter uses **Swagger**, a toolset that provides a user interface with helpful cues for how to interact with the application's RESTful APIs. 


You may choose to run ReCiter on either:
- **A server** - ReCiter will run on Linux, Mac OS X, and Windows versions 7 and higher. A minimum of 4GB of RAM is required; 16GB of RAM are recommended. An Internet connection is required to download article data from scholarly databases.
- **A local machine** - ReCiter's APIs may be run in a browser on any modern machine. The ReCiter server must be accessible to the local machine via a local area network or internet connection.

### Architecture

![https://github.com/wcmc-its/ReCiter/blob/master/files/ArchitecturalDiagram.png](https://github.com/wcmc-its/ReCiter/blob/master/files/ArchitecturalDiagram.png)


### Code repositories

The ReCiter application depends on the following separate GitHub-hosted repositories:
- **[PubMed Retrieval](https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool)**
- **[Scopus Retrieval](https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool)**
- **Data models:**
  - [ReCiter-Identity-Model](https://github.com/wcmc-its/ReCiter-Identity-Model)
  - [ReCiter-Scopus-Model](https://github.com/wcmc-its/ReCiter-Scopus-Model)
  - [ReCiter-Article-Model](https://github.com/wcmc-its/ReCiter-Article-Model)
  - [ReCiter-Dynamodb-Model](https://github.com/wcmc-its/ReCiter-Dynamodb-Model)
  - [ReCiter-PubMed-Model](https://github.com/wcmc-its/ReCiter-PubMed-Model)



## Installation

ReCiter can be installed to run locally on an AWS via a cloud formation template.

### Local

1. Clone the repository using `git clone https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool.git`
2. Go to the folder where the repository is installed and navigate to src/main/resources/application.properties and change port and log location accordingly
3. Run `mvn clean install`
4. Run `mvn spring-boot:run`. You can add additional options if you want like max and min java memory with `export MAVEN_OPTS=-Xmx1024m`
5. Go to `http://localhost:<port-number>/swagger-ui.html` to test and run any API.


### Amazon AWS

Info will be provided with forthcoming Cloud Formation Template...


## Configuration

- **PubMed API key** - Recommended for performance reasons but not necessary. More [here](https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool/blob/master/README.md#configuring-the-api-key).
- **Scopus API key and instoken** - Use of Scopus is optional. It can improve overall accuracy by several percent; Scopus is helpful because it has disambiguated organizational affiliation and verbose first name, especially for earlier articles. Use of the Scopus API is available only for Scopus subscribers. More [here](https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool/blob/master/README.md#configuring-api-key).
- **Application.properties** - All remaining configurations are stored in the [application.properties file](https://github.com/wcmc-its/ReCiter/wiki/Configuring-application.properties]. More [here](https://github.com/wcmc-its/ReCiter/wiki/Configuring-application.properties).




## Using
 
### Getting a count of articles







## Getting help

- Technical questions: please contact Sarbajit Dutta (szd2013@med.cornell.edu) or Jie Lin (jie265@gmail.com)
- Functional questions: please contact Paul Albert (paa2013@med.cornell.edu), Michael Bales (meb7002@med.cornell.edu), or publications@med.cornell.edu.

You may expect a response within one to two business days. We use GitHub issues to track bugs and feature requests. If you find a bug, please feel free to open an issue.

## Contributing

For more information about contributing, please contact Paul Albert (paa2013@med.cornell.edu) or Michael Bales (meb7002@med.cornell.edu).

## License







