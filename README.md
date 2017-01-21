# ReCiter

ReCiter is a system for disambiguating author names in publication metadata. The algorithm is described in [Johnson et al. (2014)](https://www.ncbi.nlm.nih.gov/pubmed/24694772). The first functioning version of the algorithm was implemented by Steve Johnson in object-oriented perl, was designed to operate on raw Medline data, and wrote text files as output. This updated version of ReCiter is generalized to operate on data from PubMed, and (optionally) from Scopus. ReCiter uses the same core algorithm as the previous version, but with significant revisions and updates, including new strategies to improve recall by accounting for variations in names, as well as strategies that improve accuracy by leveraging new types of data for disambiguation.

This version of ReCiter is a Representational state transfer (RESTful) web service that communicates with a local database. It may be run on a regular basis to keep publication data accurate and up-to-date.

## Introduction

See the [ReCiter wiki](https://github.com/wcmc-its/ReCiter/wiki/ReCiter-Wiki) for an introduction to ReCiter.

## Getting Started

Instructions for getting started are in the [ReCiter wiki](https://github.com/wcmc-its/ReCiter/wiki/ReCiter-Wiki).

<!--1. Install [jdk 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) or higher.
2. Import project into Eclipse, Intellij or your favorite IDE.
3. Clone the project into your local workspace by `git clone https://github.com/wcmc-its/ReCiter.git`.
4. Install the latest version of [MongoDB](https://www.mongodb.com/download-center).

More work needs to be done on the following:
5. Create a script to download data from PubMed and Scopus into MongoDB.
-->
## Getting Help

For help with ReCiter please email Jie Lin (jie265@gmail.com). You may expect a response within one to two business days. We use GitHub issues to track bugs and feature requests. If you find a bug, please contact Jie Lin or feel free to [open an issue](#opening-issues)

## Contributing

For more information about contributing, please contact Paul Albert (paa2013@med.cornell.edu) or Michael Bales (meb7002@med.cornell.edu).

## License
