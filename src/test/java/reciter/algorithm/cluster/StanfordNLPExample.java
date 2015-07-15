package reciter.algorithm.cluster;

import java.io.Reader;
import java.io.StringReader;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class StanfordNLPExample {

	public static void main(String[] args) {
		String paragraph = "My first sentence. My second sentence.";
		Reader reader = new StringReader(paragraph);
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(reader, new CoreLabelTokenFactory(), "");
		while (ptbt.hasNext()) {
			CoreLabel label = ptbt.next();
			System.out.println(label);
		}
	}
}
