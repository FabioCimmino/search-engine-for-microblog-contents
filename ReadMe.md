# ReadMe

The goal of this project is to develop a personalized search engine for microblog contents. Twitter was chosen as the microblog with the related tweets contained therein. for more details please read the pdf "Report"



### Getting Started

Download the project, If you have problems opening it, you have to import the project as Maven Project.

For starting the Sping Boot Web Application you have to:

- Run StartWebApplication.java
- Open a browser
- Go to localhost at port 8080 (localhost:8080)



### Index

The index should be placed in the root directory of this project, otherwise you have to change the index path in /ir-project/src/main/java/FabioRoberto/Backend/Processing.java at line 116 and 173.

If you want to redo the index you have to start the main of the processing.java file, calling the csv file you want inside the tok method.



### Tweet Scraping

To download tweets from twitter with twitter4j you need to set your CONSUMER_KEY and CONSUMER_SECRET as given to you by dev.twitter.com before to start TweetScraping.java at line 13 and 14.

