package main.reciter.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class YearDiscrepacyReader {

	private static Map<Integer, Double> yearDiscrepancyMap = new HashMap<Integer, Double>();

	public static void init() {
		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
        
        //initialize the CSVParser object
        CSVParser parser;
		try {
			parser = new CSVParser(new FileReader("data/DiscrepanciesYears.tab"), format);
			for(CSVRecord record : parser){
	            String year = record.get("year");
	            String score = record.get("score");
	            yearDiscrepancyMap.put(Integer.parseInt(year), Double.parseDouble(score));
	        }
	        //close the parser
	        parser.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Map<Integer, Double> getYearDiscrepancyMap() {
		return yearDiscrepancyMap;
	}

	public static void setYearDescrepancyMap(Map<Integer, Double> yearDiscrepancyMap) {
		YearDiscrepacyReader.yearDiscrepancyMap = yearDiscrepancyMap;
	}
	
	
}
