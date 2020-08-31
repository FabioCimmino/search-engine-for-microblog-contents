package FabioRoberto.searcher;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.util.ArrayList;

import static org.apache.lucene.analysis.en.EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

public class CustomAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		
		StandardTokenizer src = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(src);
        result = new LowerCaseFilter(result);
        ArrayList<String> stop = new ArrayList();
        stop.add("t.co");
        stop.add("nhttps");
        stop.add("https");
        stop.add("nhttp");
        result = new StopFilter(result, new CharArraySet(stop, true));
        result = new StopFilter (result, ENGLISH_STOP_WORDS_SET);
        result = new LengthFilter(result, 2, Integer.MAX_VALUE);
        result = new TokenFilter(result);
        result = new PorterStemFilter(result);
        return new TokenStreamComponents(src, result);
		
	}

	
}
