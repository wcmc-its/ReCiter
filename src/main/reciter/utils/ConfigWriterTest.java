package main.reciter.utils;

import java.util.Map;

import main.reciter.model.author.ReCiterAuthor;

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
		Map<String, ReCiterAuthor> map = parser.parse("data/cwid_test.data.csv");
		for (String cwid : map.keySet()) {
			configWriter.writeConfig(cwid);
		}
	}
}
