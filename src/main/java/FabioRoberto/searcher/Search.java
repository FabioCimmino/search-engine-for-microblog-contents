package FabioRoberto.searcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import FabioRoberto.queryExpansion.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Implements search functionality.
 * <p>
 * Web application needs to call {@code queryIndex()} to obtain results in json format. Call {@code close()} in app after
 * you are finished with searching.
 */
public class Search {
    // static so that they are initialized once
    private static IndexReader reader;
    private static IndexSearcher searcher;
    private static Analyzer analyzer;
    private static QueryParser parser;
    final String fieldName = "tweet";    // the field to search for
    final int MAX_RESULTS = 200;
    final int MAX_EXPANDED_QUERY_TERM_COUNT = 10;
    public String currentQuery;
    ObjectMapper jsonMapper;    // to convert to json

    public Search(String pathToIndex) throws IOException {
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(pathToIndex)));
        searcher = new IndexSearcher(reader);
        analyzer = new CustomAnalyzer();
        parser = new QueryParser(fieldName, analyzer);
        this.jsonMapper = new ObjectMapper();
    }

    /*
    public static void main(String[] args) throws IOException {
        Search search = new Search("index");
        boolean usePageRank = false;
        String queryString = "trump music";
        String expansionMethod = "Rochio";  // other options "Rochio", "Association", "None", "Metric"
        System.out.println(search.queryIndex(queryString, usePageRank, expansionMethod));
        search.close();
    }

     */

    private String createExpandedQueryString(Query query) {
        String[] expandedQueryString = query.toString().split("tweet:");
        StringBuilder sb = new StringBuilder();
        Vector<String> sbTerms = new Vector<String>();
        int count = 0;
        for (int i = 0; i < expandedQueryString.length; i++) {
            if (!sbTerms.contains(expandedQueryString[i]) &&
                    !expandedQueryString[i].chars().anyMatch(Character::isDigit) &&
                    count < MAX_EXPANDED_QUERY_TERM_COUNT) {
                count++;
                sbTerms.add(expandedQueryString[i]);
                sb.append(expandedQueryString[i]);
            }
        }
        return sb.toString();
    }

    private String getHits(Query query, boolean usePageRank, String expansionMethod) throws IOException {
        TopDocs results;
        ScoreDoc[] hits;
        int numTotalHits;
        Query originalQuery = query;    // warning: check if java modifies originalQuery if we modify query

        // Lucene's way of doing things, modify query to incorporate pagerank
        
        if (usePageRank) {
            query = getPageRankBoostedQuery(query);
        }

        // for query expansion, work with the results of regular search
        FunctionScoreQuery queryBoost = FunctionScoreQuery.boostByValue(query, DoubleValuesSource.fromIntField("retweet"));
        results = searcher.search(queryBoost, MAX_RESULTS);
        hits = results.scoreDocs;
        String finalExpandedQueryString = "";
        Query expandedQuery = null;

        // case insensitive
        String expansionMethodUpper = expansionMethod.toUpperCase();

        switch (expansionMethodUpper) {
            case "NONE":
                // no change to query
                finalExpandedQueryString = "";
                break;

            case "ROCHIO":
                TFIDFSimilarity similarity = null;
                searcher.setSimilarity(new ClassicSimilarity());
                similarity = (TFIDFSimilarity) searcher.getSimilarity();
                QueryTermVector queryTermVector = new QueryTermVector(this.currentQuery, analyzer);
                QueryExpansion queryExpansion = new QueryExpansion(analyzer, searcher, similarity);
                expandedQuery = queryExpansion.expandQuery(this.currentQuery, hits);
                finalExpandedQueryString = createExpandedQueryString(expandedQuery);
                break;

            case "ASSOCIATION":
                AssociationCluster associationCluster = new AssociationCluster(searcher, analyzer);
                expandedQuery = associationCluster.localCluster(originalQuery, hits);
                finalExpandedQueryString = createExpandedQueryString(expandedQuery);
                break;

            case "METRIC":
                MetricCluster metricCluster = new MetricCluster(searcher, analyzer);
                expandedQuery = metricCluster.localCluster(originalQuery, hits);
                finalExpandedQueryString = createExpandedQueryString(expandedQuery);
                break;

            case "SCALAR":
                ScalarCluster scalarCluster = new ScalarCluster(searcher,analyzer);
                expandedQuery = scalarCluster.localCluster(originalQuery,hits);
                finalExpandedQueryString = createExpandedQueryString(expandedQuery);
                break;

            default:
                // no change to query
                finalExpandedQueryString = "";
                break;
        }

        System.out.println(finalExpandedQueryString);
        // search again with expanded query and return the new results
        if (!"".equals(finalExpandedQueryString)) {
        	//System.out.println(!"".equals(finalExpandedQueryString));
            if (usePageRank) {
                query = getPageRankBoostedQuery(expandedQuery);
            }
            results = searcher.search(expandedQuery, MAX_RESULTS);
            System.out.println(expandedQuery);
            hits = results.scoreDocs;
        }

        numTotalHits = Math.min(Math.toIntExact(results.totalHits.value), MAX_RESULTS);  // actually these many hits
        List<Hit> resultHits = new ArrayList<>();
        for (int i = 0; i < numTotalHits; i++) {
            Document doc = searcher.doc(hits[i].doc);
            String path = doc.get("user");
            String title = doc.get("tweet");    // will be null if title doesn't exist
            String url = doc.get("url");        // will be null if url doesn't exist
            resultHits.add(new Hit(path, title, url));
        }
        QueryHit queryHit = new QueryHit(finalExpandedQueryString, resultHits);
        return finalExpandedQueryString;
    }

    /**
     * Boosts provided query using pagerank.
     *
     * @param query query to boost
     * @return boosted query
     */
    private Query getPageRankBoostedQuery(Query query) {
        Query boost = FeatureField.newLogQuery("feature", "pagerank", 0.2f, 4.5f);
        Query boostedQuery = new BooleanQuery.Builder()
                .add(query, BooleanClause.Occur.MUST)
                .add(boost, BooleanClause.Occur.SHOULD)
                .build();
        query = boostedQuery;
        return query;
    }

    /**
     * Client should call this to get results in json format.
     *
     * @param queryString the actual query entered by the user
     * @param usePageRank whether to use pagerank or not
     * @return QueryHit object
     */
    public String queryIndex(String queryString, boolean usePageRank, String expansionMethod) {
        try {
            this.currentQuery = queryString;
            Query query = parser.parse(queryString);
            String results = this.getHits(query, usePageRank, expansionMethod);
            return results;
        } catch (IOException e) {
            System.out.println("ERROR with reading index, IOException");
        } catch (ParseException e) {
            System.out.println("ERROR with parsing query, ParseException");
        }
        List<Hit> empty = new ArrayList<>();
        return "";
    }

    /**
     * Call when no need to search anymore.
     *
     * @throws IOException if there is a low-level IO error
     */
    public void close() throws IOException {
        reader.close();
    }

}
