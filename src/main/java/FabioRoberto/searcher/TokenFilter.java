package FabioRoberto.searcher;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class TokenFilter extends FilteringTokenFilter {

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	
	public TokenFilter(TokenStream in) {
		super(in);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean accept() throws IOException {
		// TODO Auto-generated method stub
		String token = new String(termAtt.buffer(), 0 ,termAtt.length());
        if (token.matches("[0-9.]+") || token.matches("[^A-Za-z]+")){
            return false;
        }
        return true;
	}

}
