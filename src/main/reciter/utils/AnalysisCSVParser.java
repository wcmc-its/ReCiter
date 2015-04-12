package main.reciter.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import main.reciter.model.author.AuthorName;
import main.reciter.model.author.ReCiterAuthor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class AnalysisCSVParser {

	public Map<String, ReCiterAuthor> parse(String fileName) {
		Map<String, ReCiterAuthor> cwidToAuthor = new HashMap<String, ReCiterAuthor>();
		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
        
        //initialize the CSVParser object
        CSVParser parser;
		try {
			parser = new CSVParser(new FileReader(fileName), format);
			for(CSVRecord record : parser){
	            String cwid = record.get("cwid");
	            String firstName = record.get("first_name");
	            String middleName = record.get("middle_name");
	            String lastName = record.get("last_name");
	            
	            if (firstName.equalsIgnoreCase("null")) {
	            	firstName = null;
	            }
	            if (middleName.equalsIgnoreCase("null")) {
	            	middleName = null;
	            }
	            if (lastName.equalsIgnoreCase("null")) {
	            	lastName = null;
	            }
	            
	            ReCiterAuthor author = new ReCiterAuthor(new AuthorName(firstName, middleName, lastName), null);
	            cwidToAuthor.put(cwid, author);
	        }
	        //close the parser
	        parser.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cwidToAuthor;
	}
}
