# ReCiter

ReCiter is a system for disambiguating author names in publication metadata. The algorithm is described in [Johnson et al. (2014)](https://github.com/wcmc-its/ReCiter/blob/master/docs/Original_ReCiter_paper.pdf). The first functioning version of the algorithm was implemented by Steve Johnson in object-oriented perl, was designed to operate on raw Medline data, and wrote text files as output. The purpose of this project is to engineer an updated version of ReCiter, which will ingest data, assign scores to indicate the likelihood a given publication was authored by a person, and allow users to maintain accurate publication lists. The scoring method will employ the same basic approach as described in the 2014 paper, but will be generalized to operate on any bibliographic database; will read and write to database files; and will leverage institution data in novel ways to boost the system's accuracy..

## Introduction

See the wikis [ReCiter wikis](https://github.com/wcmc-its/ReCiter/wiki) for an introduction to ReCiter.

## Getting Started

1. Install [jdk 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) or higher.
2. Import project into Eclipse, Intellij or your favorite IDE.
3. Clone the project into your local workspace by `git clone https://github.com/wcmc-its/ReCiter.git`.
4. Install the latest version of [MongoDB](https://www.mongodb.com/download-center).

More work needs to be done on the following:
5. Create a script to download data from PubMed and Scopus into MongoDB.

## Getting Help

Please email Jie Lin (jie265@gmail.com). We use the GitHub issues for tracking bugs and feature requests.

- If it turns out that you may have found a bug, please [open an issue](#opening-issues)

## Opening Issues

If you encounter a bug with ReCiter, we'd love to hear it. Please contact Jie Lin (jie265@gmail.com).

## Contributing

For more information about contributing, please contact Paul Albert (paa2013@med.cornell.edu) or Michael Bales (meb2007@med.cornell.edu).

## License
