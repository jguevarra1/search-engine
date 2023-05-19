Search Engine
=================================================
## Description
This project is a simple full stack search engine webapp that displays X amount of relevant search results given a seed URL. It utilizes a multi-threaded inverted index to store an HTML webpage's word stems, their file location, and the position in the file. When a search query is entered from the UI, depending on the search flag provided, either an exact or partial search will be made against the inverted index. A webcrawler is used to crawl an HTML webpage for other links to traverse, storing already traversed links into a list to avoid re-crawling, until the crawler hits the max number of links to crawl. 

Relevance is calculated based on 
1) Score (Total number of matches divided by total number of words in the file)
2) Raw Count (if scores are the same, then total number of words dictates relevancy)
3) Location (If both Score and Raw Count are the same, then path location by alphabetical order)

The dynamic front end is made using Jetty, Servlets, HTML, and the Bulma CSS framework.

![search-engine-ui](https://github.com/jguevarra1/search-engine/assets/73259113/ed81659d-fb3e-488c-81f2-97929df75a53)

## How to Use

The code uses an ArgumentMap class to parse command line arguments. In order to run this project, clone the repo and open the project in your Eclipse IDE. Once the project finishes loading, open the driver class, go to the top left of your screen and into the "Run" tab to run the project with your custom configurations. Into the "Arguments" tab, enter in your command-line arguments into the "Program arguments" box. They may be provided in any order. 

The input flags for this project are as follows:
1) **-html seed** where **-html** indicates the next argument seed is the seed URL your web crawler should initially crawl to build the inverted index
2) **-max total** where **-max** is an optional flag that indicates the next argument total is the total number of URLs to crawl (including the seed URL) when building the index (1 is the default number)
3) **-server port** where **-server** indicates a search engine web server should be launched and the next argument port is the port the web server should use to accept socket connections (8080 is used as default)
4) **-threads num** threads where **-threads** indicates the next argument num is the number of worker threads to use. (If num is missing or there is an invalid number, 5 threads is used as default)
5) **-exact** which is an optional flag to perform exact searches (by default the project performs partial searching)

An example configuration would be "-html https://example.com/some-directory/ -server 8080 -max 50 -threads 3" 
1) example.com/some-directory is our seed URL
2) 8080 is the port we want to host the applicaiton on
3) 50 is the max number of URLs we want our webcrawler to crawl
4) 3 is the number of threads

![example-run](https://github.com/jguevarra1/search-engine/assets/73259113/8e6bb12a-76f3-4b5b-9019-d1be52e0d940)

This is what an example run looks like when a search is entered and the results are displayed:

![search-engine-example](https://github.com/jguevarra1/search-engine/assets/73259113/8e57871d-0c97-448f-afa2-3df43c74833b)

 
