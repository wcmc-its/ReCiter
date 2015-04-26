package test.examples.pubmed;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class CosineSimilarityTest {
	
	public static void main(String[] args) {
        try {
        	CosineSimilarityTest cosSim = new 
        			CosineSimilarityTest(
        	"Weill Cornell Medical College, New York, NY, USA. adc9001@med.cornell.edu", 
            "Department of Herpetology, Department of Ornithology, American Museum of Natural History, Central Park West at 79th Street, New York, New York, 10024-5192, USA." );
            System.out.println( cosSim.getCosineSimilarity() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final String CONTENT = "Content";
    public static final int N = 2;//Total number of documents

    private final Set<String> terms = new HashSet<>();
    private final RealVector v1;
    private final RealVector v2;

    CosineSimilarityTest(String s1, String s2) throws IOException {
        Directory directory = createIndex(s1, s2);
        IndexReader reader = DirectoryReader.open(directory);
        Map<String, Double> f1 = getWieghts(reader, 0);
        Map<String, Double> f2 = getWieghts(reader, 1);
        reader.close();
        v1 = toRealVector(f1);
        System.out.println( "V1: " +v1 );
        v2 = toRealVector(f2);
        System.out.println( "V2: " +v2 );
    }

    Directory createIndex(String s1, String s2) throws IOException {
        Directory directory = new RAMDirectory();
        Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_CURRENT);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_CURRENT,
                analyzer);
        IndexWriter writer = new IndexWriter(directory, iwc);
        addDocument(writer, s1);
        addDocument(writer, s2);
        writer.close();
        return directory;
    }

    /* Indexed, tokenized, stored. */
    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_STORED.setIndexed(true);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(true);
        TYPE_STORED.freeze();
    }

    void addDocument(IndexWriter writer, String content) throws IOException {
        Document doc = new Document();
        Field field = new Field(CONTENT, content, TYPE_STORED);
        doc.add(field);
        writer.addDocument(doc);
    }

    double getCosineSimilarity() {
        double dotProduct = v1.dotProduct(v2);
        System.out.println( "Dot: " + dotProduct);
        System.out.println( "V1_norm: " + v1.getNorm() + ", V2_norm: " + v2.getNorm() );
        double normalization = (v1.getNorm() * v2.getNorm());
        System.out.println( "Norm: " + normalization);
        return dotProduct / normalization;
    }


    Map<String, Double> getWieghts(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, CONTENT);
        Map<String, Integer> docFrequencies = new HashMap<>();
        Map<String, Integer> termFrequencies = new HashMap<>();
        Map<String, Double> tf_Idf_Weights = new HashMap<>();
        TermsEnum termsEnum = null;
        DocsEnum docsEnum = null;


        termsEnum = vector.iterator(termsEnum);
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int docFreq = termsEnum.docFreq();
            docFrequencies.put(term, reader.docFreq( new Term( CONTENT, term ) ));

            docsEnum = termsEnum.docs(null, null);
            while (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                termFrequencies.put(term, docsEnum.freq());
            }

            terms.add(term);
        }

        for ( String term : docFrequencies.keySet() ) {
            int tf = termFrequencies.get(term);
            int df = docFrequencies.get(term);
            double idf = ( 1 + Math.log(N) - Math.log(df) );
            double w = tf * idf;
            tf_Idf_Weights.put(term, w);
            //System.out.printf("Term: %s - tf: %d, df: %d, idf: %f, w: %f\n", term, tf, df, idf, w);
        }

        System.out.println( "Printing docFrequencies:" );
        printMap(docFrequencies);

        System.out.println( "Printing termFrequencies:" );
        printMap(termFrequencies);

        System.out.println( "Printing if/idf weights:" );
        printMapDouble(tf_Idf_Weights);
        return tf_Idf_Weights;
    }

    RealVector toRealVector(Map<String, Double> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        double value = 0;
        for (String term : terms) {

            if ( map.containsKey(term) ) {
                value = map.get(term);
            }
            else {
                value = 0;
            }
            vector.setEntry(i++, value);
        }
        return vector;
    }

    public static void printMap(Map<String, Integer> map) {
        for ( String key : map.keySet() ) {
            System.out.println( "Term: " + key + ", value: " + map.get(key) );
        }
    }

    public static void printMapDouble(Map<String, Double> map) {
        for ( String key : map.keySet() ) {
            System.out.println( "Term: " + key + ", value: " + map.get(key) );
        }
    }
}
