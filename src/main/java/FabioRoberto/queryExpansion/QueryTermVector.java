package FabioRoberto.queryExpansion; /**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;


public class QueryTermVector {
    private String[] terms = new String[0];
    private int[] termFreqs = new int[0];

    /**
     *
     * @param queryTerms The original list of terms from the query, can contain duplicates
     */
    public QueryTermVector(String[] queryTerms) {

        processTerms(queryTerms);
    }

    public QueryTermVector(String queryString, Analyzer analyzer) throws IOException {
        if (analyzer != null) {
            QueryParser parser = new QueryParser("tweet", analyzer);
            QueryParser.escape(queryString);

            TokenStream stream = analyzer.tokenStream("tweet", new StringReader(queryString));
            if (stream != null) {
                stream.reset();
                OffsetAttribute offsetAttribute = stream.getAttribute(OffsetAttribute.class);
                CharTermAttribute termAttribute = stream.getAttribute(CharTermAttribute.class);
                List<String> terms = new ArrayList<String>();
                boolean hasMoreTokens = false;
                try {
                    while (stream.incrementToken()) {
                        int startOffset = offsetAttribute.startOffset();
                        int endOffset = offsetAttribute.endOffset();
                        terms.add(termAttribute.toString());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                stream.reset();

//          stream.reset();
//          TermAttribute termAtt = stream.addAttribute(TermAttribute.class);
//          hasMoreTokens = stream.incrementToken();
//          while (hasMoreTokens) {
//            terms.add(termAtt.term());
//            hasMoreTokens = stream.incrementToken();
//          }

                processTerms(terms.toArray());
//          processTerms(terms.toArray(new String[terms.size()]));
            }
        }
    }

    public String getField() {
        return null;
    }

    private void processTerms(Object[] queryTerms) {
        if (queryTerms != null) {
            Arrays.sort(queryTerms);
            Map<String, Integer> tmpSet = new HashMap<String, Integer>(queryTerms.length);
            //filter out duplicates
            List<String> tmpList = new ArrayList<String>(queryTerms.length);
            List<Integer> tmpFreqs = new ArrayList<Integer>(queryTerms.length);
            int j = 0;
            for (int i = 0; i < queryTerms.length; i++) {
                String term = (String) queryTerms[i];
                Integer position = tmpSet.get(term);
                if (position == null) {
                    tmpSet.put(term, Integer.valueOf(j++));
                    tmpList.add(term);
                    tmpFreqs.add(Integer.valueOf(1));
                } else {
                    Integer integer = tmpFreqs.get(position.intValue());
                    tmpFreqs.set(position.intValue(), Integer.valueOf(integer.intValue() + 1));
                }
            }
            terms = tmpList.toArray(terms);
            //termFreqs = (int[])tmpFreqs.toArray(termFreqs);
            termFreqs = new int[tmpFreqs.size()];
            int i = 0;
            for (final Integer integer : tmpFreqs) {
                termFreqs[i++] = integer.intValue();
            }
        }
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; i < terms.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(terms[i]).append('/').append(termFreqs[i]);
        }
        sb.append('}');
        return sb.toString();
    }


    public int size() {
        return terms.length;
    }

    public String[] getTerms() {
        return terms;
    }

    public int[] getTermFrequencies() {
        return termFreqs;
    }

    public int indexOf(String term) {
        int res = Arrays.binarySearch(terms, term);
        return res >= 0 ? res : -1;
    }

    public int[] indexesOf(String[] terms, int start, int len) {
        int[] res = new int[len];

        for (int i = 0; i < len; i++) {
            res[i] = indexOf(terms[i]);
        }
        return res;
    }

}

