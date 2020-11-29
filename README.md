# ReCiter
![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiVWFoRUNZQzBqMkJpTGQ4MHNNbUJkclE4OTRVV0Y0SzJBVTBVMmV5NVhyNUdpbXVDblkrcWZkWU9SQWFxV0dUand4d05iVFFlT0Z0bWJsamM1SnRaUFdFPSIsIml2UGFyYW1ldGVyU3BlYyI6ImkzMndTalROWWlXUC8yRW4iLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)
![version](https://img.shields.io/badge/version-1.0-blue.svg?maxAge=2592000)
[![codebeat badge](https://codebeat.co/badges/9845c96b-ed87-4b1a-b62c-1e0e8b51bbb8)](https://codebeat.co/projects/github-com-wcmc-its-reciter-master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![Pending Pull-Requests](https://img.shields.io/github/issues-pr-raw/wcmc-its/reciter.svg?color=blue)](https://github.com/wcmc-its/ReCiter/pulls)
[![Closed Pull-Requests](https://img.shields.io/github/issues-pr-closed-raw/wcmc-its/reciter.svg?color=blue)](https://github.com/wcmc-its/ReCiter/pulls)
[![GitHub issues open](https://img.shields.io/github/issues-raw/wcmc-its/reciter.svg?maxAge=2592000)](https://github.com/wcmc-its/ReCiter/issues)
[![GitHub issues closed](https://img.shields.io/github/issues-closed-raw/wcmc-its/reciter.svg?maxAge=2592000)](https://github.com/wcmc-its/ReCiter/issues)
[![star this repo](http://githubbadges.com/star.svg?user=wcmc-its&repo=ReCiter&style=flat)](https://github.com/wcmc-its/ReCiter)
[![fork this repo](http://githubbadges.com/fork.svg?user=wcmc-its&repo=ReCiter&style=flat)](https://github.com/wcmc-its/ReCiter/fork)
[![Tags](https://img.shields.io/github/tag/wcmc-its/reciter.svg?style=social)](https://github.com/wcmc-its/ReCiter/releases)
[![Github All Releases](https://img.shields.io/github/downloads/wcmc-its/ReCiter/total.svg)]()
[![Open Source Love](https://badges.frapsoft.com/os/v3/open-source.svg?v=102)](https://github.com/wcmc-its/ReCiter/) 

- [Purpose](#purpose)
- [Technical](#technical)
  - [Prerequisites](#prerequisites)
  - [Technological stack](#technological-stack)
  - [Architecture](#architecture)
  - [Related code repositories](#related-code-repositories)
- [Installation](#installation)
  - [Local](#local)
  - [Amazon AWS](#amazon-aws)
- [Configuration](#configuration)
- [Functionality](#functionality)
  - [How ReCiter works](#how-reciter-works)
  - [Using the APIs](#using-the-apis)
  - [See also](#see-also)
- [Future work](#future-work)
- [Follow up](#follow-up)


![https://github.com/wcmc-its/ReCiter/blob/master/files/howreciterworks.png](https://github.com/wcmc-its/ReCiter/blob/master/files/howreciterworks.png)


## Purpose

ReCiter is a highly accurate system for guessing which publications in PubMed a given person has authored. ReCiter includes a Java application, a DynamoDB-hosted database, and a set of RESTful microservices which collectively allow institutions to maintain accurate and up-to-date author publication lists for thousands of people. This software is optimized for disambiguating authorship in PubMed and, optionally, Scopus.

ReCiter accurately identifies articles, including those at previous affiliations, by a given person. It does this by leveraging institutionally maintained identity data (e.g., departments, relationships, email addresses, year of degree, etc.) With the more complete and efficient searches that result from combining these types of data, you can save time and your institution can be more productive. If you run ReCiter daily, you can ensure that the desired users are the first to learn when a new publication has appeared in PubMed. 

ReCiter is fast. It uses an advanced multi-threading strategy known as a [work stealing pool](https://en.wikipedia.org/wiki/Work_stealing) to make up to 10 retrieval requests at a time.

ReCiter is freely available and open source under the [Apache 2.0 license](https://opensource.org/licenses/Apache-2.0).  

Please see the [ReCiter wiki](https://github.com/wcmc-its/ReCiter/wiki) for more information.

![https://github.com/wcmc-its/ReCiter/blob/master/files/ReCiter-FeatureGenerator.gif](https://github.com/wcmc-its/ReCiter/blob/master/files/ReCiter-FeatureGenerator.gif)



## Technical 

### Prerequisites
- Java 1.8
- Latest version of Maven. To install Maven navigate to the directory where ReCiter will be installed, execute `brew install maven` and then `mvn clean install`

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

![https://github.com/wcmc-its/ReCiter/blob/master/files/ArchitecturalDiagram-NEW.png](https://github.com/wcmc-its/ReCiter/blob/master/files/ArchitecturalDiagram-NEW.png)


### Related code repositories

The ReCiter application depends on the following separate GitHub-hosted repositories:
- **[PubMed Retrieval Tool](https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool)**
- **[Scopus Retrieval Tool](https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool)** (optional)
- **Data models:**
  - [ReCiter-Identity-Model](https://github.com/wcmc-its/ReCiter-Identity-Model)
  - [ReCiter-Scopus-Model](https://github.com/wcmc-its/ReCiter-Scopus-Model)
  - [ReCiter-Article-Model](https://github.com/wcmc-its/ReCiter-Article-Model)
  - [ReCiter-Dynamodb-Model](https://github.com/wcmc-its/ReCiter-Dynamodb-Model)
  - [ReCiter-PubMed-Model](https://github.com/wcmc-its/ReCiter-PubMed-Model)

Additionally, users can install:
- [ReCiter Publication Manager](https://github.com/wcmc-its/ReCiter-Publication-Manager) - a user interface tool
- [ReCiter Machine Learning / Analysis](https://github.com/wcmc-its/ReCiter-MachineLearning-Analysis) - a suite of scripts and tools for retrieving and analyzing data from ReCiter

## Installation

ReCiter can be installed to run locally on an AWS via a cloud formation template. A required dependency is the [PubMed Retrieval Tool](https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool/). The [Scopus Retrieval Tool](https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool/) is optional, but can have improve overall accuracy by several percent. 

### Local

1. Clone the repository to a local folder using `git clone https://github.com/wcmc-its/ReCiter.git`
2. Go to the folder where the repository has been cloned and navigate to src/main/resources/application.properties and change port and log location accordingly
- change `aws.DynamoDb.local=false` to `aws.DynamoDb.local=true`
- update location of DynamoDB database, e.g., `aws.DynamoDb.local.dbpath=/Users/Paul/Documents/ReCiter/dynamodb_local_latest`
- By default application security is turned on. If you wish to turn it off you must change the flag to false from `spring.security.enabled=true` to `spring.security.enabled=false`
- If you have the security as true you must include the following environment variables - 
```
export ADMIN_API_KEY=<api-key>
export CONSUMER_API_KEY=<api-key>
```
- If you do not have scopus subscription you should mark this value to false. Change `use.scopus.articles=true` to `use.scopus.articles=false`.
3. Enter ports for server and services in command line. Note that the Scopus service is optional. You must have [Pubmed Service](https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool/) and optionally [Scopus Service](https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool/) setup before this step. Enter appropriate hostname and the port numbers.
```
export SERVER_PORT=5000
export SCOPUS_SERVICE=http://localhost:5001
export PUBMED_SERVICE=http://localhost:5002
```
4. Run `mvn spring-boot:run`. You can add additional options if you want like max and min java memory with `export MAVEN_OPTS=-Xmx1024m`
5. Go to `http://localhost:<port-number>/swagger-ui.html` to test and run any API.


### Amazon AWS

The [ReCiter CloudFormation](
https://github.com/wcmc-its/ReCiter-CloudFormation) template allows you to use a simple text file to model and provision, in an automated and secure manner, all the resources needed for your applications across all regions and accounts. This file serves as the single source of truth for your cloud environment. There you will find instruction to install ReCiter and its components.




## Configuration

- **[PubMed API key](https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool/blob/master/README.md#configuring-the-api-key)** - Recommended for performance reasons and to prevent and limit the likelihood National Library of Medicine will throttle you, but otherwise not necessary.  
- **[Scopus API key and instoken](https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool/blob/master/README.md#configuring-api-key)** - Use of Scopus is optional. It can improve overall accuracy by several percent; Scopus is helpful because it has disambiguated organizational affiliation and verbose first name, especially for earlier articles. Use of the Scopus API is available only for Scopus subscribers. 
- **[Security](https://github.com/wcmc-its/ReCiter/wiki/Implementing-security)** - Each of ReCiter's APIs can be configured to restrict access to only those requests which provide the correct API key.
- **[Application.properties](https://github.com/wcmc-its/ReCiter/wiki/Configuring-application.properties)** - All remaining configurations are stored here.



## Functionality

### How ReCiter works

The wiki article, [How ReCiter works](https://github.com/wcmc-its/ReCiter/wiki/How-ReCiter-works), contains a more detailed description on the application works. 
 
 - Populate identity information for target users
 - Optional: populate Gold Standard of already accepted or rejected publications; note that this system currently does not offer a user interface for collecting this feedback
 - Lookup candidate articles in PubMed and, optionally, Scopus
 - Compute suggestions
 - Retrieve suggestions
 


### Using the APIs

The wiki article, [Using the APIs](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs), contains a full description on how to use the ReCiter APIs.


|Category |Function |Relevant API(s) |
| ---- | ------------- | ------------- |
|Manage identity of target users | Add or update identity data for target user(s) from Identity table | [`/reciter/identity/`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#reciteridentity) or [`/reciter/save/identities/`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#recitersaveidentities) |
|Manage identity of target users | Retrieve identity data for target user(s) from Identity table | [`/reciter/find/identity/by/uid/`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#reciterfindidentitybyuid) or [`/reciter/find/identity/by/uids/`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#reciterfindidentitybyuids) |
|Gold standard | Update the GoldStandard table (includes both accepted and rejected PMIDs) for single user | [`/reciter/goldstandard/`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs##recitergoldstandard-post) |
|Gold standard | Update the GoldStandard table (includes both accepted and rejected PMIDs) for mutliple users | [`/reciter/goldstandard/`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs##recitergoldstandard-put) |
|Gold standard | Read from the GoldStandard table (includes both accepted and rejected PMIDs) for target user(s) | [`/reciter/goldstandard/{uid}`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#recitergoldstandarduid)  |
| Look up candidate articles | Trigger look up of candidate articles for a given user | [`/reciter/retrieve/articles/by/uid`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#reciterretrievearticlesbyuid)  |
| Retrieve suggested articles  | Read suggested articles from the Analysis table for target user | [`/reciter/article-retrieval/by/uid`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#reciterarticle-retrievalbyuid)   | 
| Retrieve suggested articles | Read suggested articles and see supporting evidence from the Analysis table for target user(s) | [`/reciter/feature-generator/by/uid`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#reciterfeature-generatorbyuid) or [`/reciter/feature-generator/by/group`](https://github.com/wcmc-its/ReCiter/wiki/Using-the-APIs#reciterfeature-generatorbygroup)  | 




### See also

- [PubMed Retrieval Tool](https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool) - contains several ways you can search PubMed
- [Scopus Retrieval Tool](https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool) - contains several ways you can search Scopus



## Future work

Both the issue queue and the [Roadmap](https://github.com/wcmc-its/ReCiter/wiki/Roadmap) include some areas where we want to improve ReCiter.


## Follow up

- Technical questions: contact [Sarbajit Dutta](mailto:szd2013@med.cornell.edu) or [Jie Lin](mailto:jie265@gmail.com)
- Functional questions: contact [Paul Albert](mailto:paa2013@med.cornell.edu), [Michael Bales](mailto:meb7002@med.cornell.edu), or publications@med.cornell.edu.

You may expect a response within one to two business days. We use GitHub issues to track bugs and feature requests. If you find a bug, please feel free to open an issue.

Contributions welcome!





