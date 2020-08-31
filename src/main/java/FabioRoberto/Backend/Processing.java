package FabioRoberto.Backend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class Processing {
	

	public static void tok (String pathName) throws IOException {
		
		BufferedReader BR;
		BR = new BufferedReader(new FileReader(pathName));
		
		DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	    DateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
	    inputFormat.setLenient(true);
	    
		String riga;
		
		//analyzer 
				Map<String,Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
				analyzerPerField.put("tweet", new CustomAnalyzer());
				
				PerFieldAnalyzerWrapper aWrapper= new PerFieldAnalyzerWrapper(new SimpleAnalyzer(),analyzerPerField);
				Path indexPath = Files.createTempDirectory("ProgettoIR");
				Directory directory=FSDirectory.open(indexPath);
				IndexWriterConfig config=new IndexWriterConfig(aWrapper);
				IndexWriter iwriter=new IndexWriter(directory,config);
				
		while(  (riga=BR.readLine()) != null ){

			if(!riga.isEmpty()){
				String[] fields = riga.split("\t");

				if (fields.length == 6 ) {

					Date date = null;
					try {
						date = inputFormat.parse(fields[0]);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					String outputText = outputFormat.format(date);

					Document doc = new Document();
					doc.add(new SortedDocValuesField("date", new BytesRef(outputText)));
					doc.add(new StoredField("date", outputText));

					StringField nameField = new StringField("name",fields[1],Field.Store.YES);
					doc.add(nameField);

					TextField tweetField = new TextField("tweet",fields[2],Field.Store.YES);
					doc.add(tweetField);

					Integer likes =  Integer.parseInt(fields[3]);
					doc.add(new NumericDocValuesField("likes", likes));
					doc.add(new StoredField("likes", likes));

					Integer retweet =  Integer.parseInt(fields[4]);
					doc.add(new NumericDocValuesField("retweet", retweet));
					doc.add(new StoredField("retweet", retweet));

					StoredField urlField = new StoredField("url",fields[5]);
					doc.add(urlField);

					iwriter.addDocument(doc);
				}
			}
			
		}
		iwriter.close();
	}
	
	public ArrayList <Tweet> query (String path, String q) {
		//String pat = System.getProperty("user.dir")+"\\"+path+"\\";
		//Path pathIndex = Paths.get("/Users/roberto/Downloads/SpringApplication/Index");

		Path currentRelativePath = Paths.get(path);
		Path pathIndex = currentRelativePath.toAbsolutePath();

        Directory dr = null;
		
			try {
				dr = FSDirectory.open(pathIndex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        IndexReader indexReader = null;
		try {
			indexReader = DirectoryReader.open(dr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser("tweet", new CustomAnalyzer());
      
			Query query;
	try {
		query = queryParser.parse(q);
		TopDocs topDocs = null;
        
		topDocs = indexSearcher.search(query, 50);
        Document doc = new Document();
        ArrayList <Tweet> tweet= new ArrayList <Tweet> ();
         for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
 			doc = indexReader.document(scoreDoc.doc);
           //  System.out.println(doc.getField("likes").stringValue() +"  " +doc.getField("tweet").stringValue()  );
             tweet.add(new Tweet(doc.getField("tweet").stringValue(), doc.getField("url").stringValue(), doc.getField("date").stringValue(),
            		Integer.parseInt( doc.getField("retweet").stringValue()),Integer.parseInt( doc.getField("likes").stringValue())));
            
         }
         return tweet;
         
			} catch (org.apache.lucene.queryparser.classic.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	return null;
	
	
	}
	
	public ArrayList <Tweet> personalized_query (String path, String q,String expansion, User user,String category, String relevance,String userScelto) {
		//String pat = System.getProperty("user.dir")+"\\"+path+"\\";
		//Path pathIndex = Paths.get("/Users/roberto/Downloads/SpringApplication/Index");

		Path currentRelativePath = Paths.get(path);
		Path pathIndex = currentRelativePath.toAbsolutePath();
		//System.out.println("Current relative path is: " + pathIndex);

        Directory dr = null;
			try {
				dr = FSDirectory.open(pathIndex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        IndexReader indexReader = null;
		try {
			indexReader = DirectoryReader.open(dr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //StandardAnalyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser("tweet", new CustomAnalyzer());
        Query query, q1 = null;

	try {
		query = queryParser.parse(q);

		String queryUserProfile="";
		switch (category) {
		case "news": 
			
			for (int i =0; i<user.getNewsPref().size(); i++)
				queryUserProfile+=" "+ user.getNewsPref().get(i);
			q1= queryParser.parse(queryUserProfile);
			break;
			
		case "cinema": 
			for (int i =0; i<user.getCinemaPref().size(); i++)
				queryUserProfile+=" "+ user.getCinemaPref().get(i);
			q1= queryParser.parse(queryUserProfile);
			break;
			
		case "science": 
			for (int i =0; i<user.getSciencePref().size(); i++)
				queryUserProfile+=" "+ user.getSciencePref().get(i);
			q1= queryParser.parse(queryUserProfile);
			break;
			
		case "sport": 
			for (int i =0; i<user.getSportPref().size(); i++)
				queryUserProfile+=" "+ user.getSportPref().get(i);
			q1= queryParser.parse(queryUserProfile);
			break;
			
		case "music": 
			for (int i =0; i<user.getMusicPref().size(); i++)
				queryUserProfile+=" "+ user.getMusicPref().get(i);
			q1= queryParser.parse(queryUserProfile);
			break;
		case "none":
			if (!userScelto.equals("none"))
			{	
			ArrayList <String> allPref = new ArrayList<String>();
			allPref.addAll(user.getCinemaPref());
			allPref.addAll(user.getMusicPref());
			allPref.addAll(user.getNewsPref());
			allPref.addAll(user.getSportPref());
			allPref.addAll(user.getSciencePref());
			for (int i =0; i<allPref.size(); i++)
				queryUserProfile+=allPref.get(i)+" ";
			q1= queryParser.parse(queryUserProfile);
			System.out.println(q1);
			}
			break;
		}


		FunctionScoreQuery queryBoost = null;
		TopDocs topDocs = null;

		if (!expansion.equals("")){
			Query query_expansion = queryParser.parse(expansion);
			switch (relevance) {
				case "retweet":
					if (!category.equals("none")  || !userScelto.equals("none")) {
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(q1, BooleanClause.Occur.SHOULD)
								.add(query_expansion, BooleanClause.Occur.SHOULD)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("retweet"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}
					else
					{
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(query_expansion, BooleanClause.Occur.SHOULD)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("retweet"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}

					break;
				case "likes":
					if (!category.equals("none")  || !userScelto.equals("none")) {
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(q1, BooleanClause.Occur.SHOULD)
								.add(query_expansion, BooleanClause.Occur.SHOULD)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("likes"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}
					else
					{
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(query_expansion, BooleanClause.Occur.SHOULD)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("likes"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}
					break;
				case "none":
					if (!category.equals("none") || !userScelto.equals("none") ) {
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(q1, BooleanClause.Occur.SHOULD)
								.add(query_expansion, BooleanClause.Occur.SHOULD)
								.build();
						topDocs = indexSearcher.search(queryB, 50);
						System.out.println(queryB);
					}
					else
					{
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(query_expansion, BooleanClause.Occur.SHOULD)
								.build();
						topDocs = indexSearcher.search(queryB, 50);
						System.out.println(queryB);
					}
					break;
			}

		}
		else {

			switch (relevance) {
				case "retweet":
					if (!category.equals("none")  || !userScelto.equals("none")) {
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(q1, BooleanClause.Occur.SHOULD)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("retweet"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}
					else
					{
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("retweet"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}

					break;
				case "likes":
					if (!category.equals("none")  || !userScelto.equals("none")) {
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(q1, BooleanClause.Occur.SHOULD)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("likes"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}
					else
					{
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.build();
						queryBoost = FunctionScoreQuery.boostByValue(queryB, DoubleValuesSource.fromIntField("likes"));
						topDocs = indexSearcher.search(queryBoost, 50);
						System.out.println(queryBoost);
					}
					break;
				case "none":
					if (!category.equals("none") || !userScelto.equals("none") ) {
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.add(q1, BooleanClause.Occur.SHOULD)
								.build();
						topDocs = indexSearcher.search(queryB, 50);
						System.out.println(queryB);
					}
					else
					{
						BooleanQuery queryB = new BooleanQuery.Builder()
								.add(query, BooleanClause.Occur.MUST)
								.build();
						topDocs = indexSearcher.search(queryB, 50);
						System.out.println(queryB);
					}
					break;
			}
		}


        Document doc = new Document();
        ArrayList <Tweet> tweet= new ArrayList <Tweet> ();
         for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
 			doc = indexReader.document(scoreDoc.doc);
            // System.out.println(doc.getField("tweet").stringValue());
 			  tweet.add(new Tweet(doc.getField("tweet").stringValue(), doc.getField("url").stringValue(), doc.getField("date").stringValue(),
 	            		Integer.parseInt( doc.getField("retweet").stringValue()),Integer.parseInt( doc.getField("likes").stringValue()))); 
         }
         return tweet;
         
			} catch (org.apache.lucene.queryparser.classic.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	return null;
	}

	public User userFactory(String pathName) throws IOException {
		BufferedReader BR;
		BR = new BufferedReader(new FileReader(pathName));

		String riga;
		User user = new User();
		ArrayList <String> cinema = new ArrayList<String>();
		ArrayList <String> news = new ArrayList<String>();
		ArrayList <String> sport = new ArrayList<String>();
		ArrayList <String> science = new ArrayList<String>();
		ArrayList <String> music = new ArrayList<String>();
		int num_riga = 0;

		while(  (riga=BR.readLine()) != null ) {

			if (!riga.isEmpty()) {
				String[] fields = riga.split(",");

				if (num_riga==0){
					for (int i = 0; i<fields.length; i++ ){
						cinema.add(fields[i]);
					}
					user.setCinemaPref(cinema);
				}
				else if (num_riga==1){
					for (int i = 0; i<fields.length; i++ ){
						news.add(fields[i]);
					}
					user.setNewsPref(news);
				}
				else if (num_riga==2){
					for (int i = 0; i<fields.length; i++ ){
						sport.add(fields[i]);
					}
					user.setSportPref(sport);
				}
				else if (num_riga==3){
					for (int i = 0; i<fields.length; i++ ){
						science.add(fields[i]);
					}
					user.setSciencePref(science);
				}
				else if (num_riga==4){
					for (int i = 0; i<fields.length; i++ ){
						music.add(fields[i]);
					}
					user.setMusicPref(music);
				}

				num_riga++;
			}

		}
		return user;
	}

	public ArrayList <String> filteredBy(User user, String category) {
		switch(category){
			case "cinema": return user.getCinemaPref();
			case "music": return user.getMusicPref();
			case "sport": return user.getSportPref();
			case "science": return user.getSciencePref();
			case "news": return user.getNewsPref();
		}
		ArrayList <String> allPrefs = new ArrayList <String>();
		allPrefs.addAll(user.getCinemaPref());
		allPrefs.addAll(user.getMusicPref());
		allPrefs.addAll(user.getSportPref());
		allPrefs.addAll(user.getSciencePref());
		allPrefs.addAll(user.getNewsPref());

		return allPrefs;
	}
	
	public static void main (String [] args) throws IOException {
		tok("tweets.csv");
	}

}

