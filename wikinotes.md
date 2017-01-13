## Why use ReCiter? 
The purpose of ReCiter is to enable rapid and accurate searching for articles by specific authors.

We feel that our method of doing this can save time and money over the more manual approaches currently used to do this.  

ReCiter does this by filtering an author name search with additional data such as: 
	
* name variants (such as nicknames, name changes, spelling irregularities, etc) 
* current and former institutional affiliation and department
* e-mail addresses
* citizenship
* graduation year
* co-authorship 
* journal publication patterns

ReCiter's accuracy has been measured at over 95% for current full-time faculty at Weill Cornell Medicine.

For example:  Suppose you wish to search for articles by Andrew Schwartz ( not his real name!) who currently works at Harvard, but used to work at Union County Community College.  

The PubMed interface will retrieve over 3,500 results when searching for "A Schwartz" as an author because it will show results for all people who are named "A. Schwartz".  If you want results for *our* A. Schwartz, you *could* filter the search by Harvard University, but then would have to search again for works written for other institutions.  Additionally, you would have to research A. Schwartz to identify those past affiliations.

The ReCiter system can handle search for you in one go, quickly and accurately.

## How do I use ReCiter? 
We are currently working on a GUI front end for ReCiter.  

As of this moment, ReCiter provides a JSON data feed for all searches and inquiries against its database.

We support two methods of working with ReCiter:

* **stand-alone**, for users only, and
* **with Eclipse IDE** for those who want to help develop ReCiter or read its source code.

### Installation as Stand-Alone:
#### Prerequisites: 
1. Java version 8+
2. Spring Boot framework.

## WE NEED TO : 
* explain how to install Maven and Spring-Boot and what they are.
* explain java install
* directions for Windows ( install bash? or instructs for win shell ) 
Try installing on a 'naive computer'

**ALSO System requirements.**


1. Open a bash terminal.
2. Navigate to an installation directory of your choosing.
3. Clone the source with: ```git clone https://github.com/wcmc-its/ReCiter.git```
4. Enter the cloned directory with: ```cd ReCiter```
5. 










Currently, all requests create a JSON formatted data set which you can use for any 

In order to fully use ReCiter, you will need 
