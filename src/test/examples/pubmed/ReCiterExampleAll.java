package test.examples.pubmed;

import java.io.IOException;
import java.util.Map;

import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.AnalysisCSVParser;
import main.reciter.utils.ReCiterConfigProperty;

public class ReCiterExampleAll {

	public static void main(String[] args) throws IOException {
		
		run();
	}
	
	
	public static void run() throws IOException {
		AnalysisCSVParser parser = new AnalysisCSVParser();
		Map<String, ReCiterAuthor> map = parser.parse("cwid_test.data.csv");
		
		
		for (String cwid : map.keySet()) {
			ReCiterExample example = new ReCiterExample();
			ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
			reCiterConfigProperty.loadProperty("data/properties/" + cwid + "/" + cwid + ".properties");
			example.runExample(reCiterConfigProperty);
		}
	}
}
