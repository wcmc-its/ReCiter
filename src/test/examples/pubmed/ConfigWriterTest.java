package test.examples.pubmed;

import java.util.Map;

import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.AnalysisCSVParser;
import main.reciter.utils.ConfigWriter;

/**
 * Writes the configuration properties for all cwids in "cwid_test.data.csv".
 * @author jil3004
 *
 */
public class ConfigWriterTest {

	public static void main(String[] args) {
		writeConfigAll();
	}
	
	public static void writeConfigAll() {
		ConfigWriter configWriter = new ConfigWriter();
		AnalysisCSVParser parser = new AnalysisCSVParser();
		Map<String, ReCiterAuthor> map = parser.parse("cwid_test.data.csv");
		for (String cwid : map.keySet()) {
			configWriter.writeConfig(cwid);
		}
	}
}
