package reciter.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReCiterEngineProperty {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterEngineProperty.class);

	private static String reCiterPropertyFile = "src/main/resources/config/reciter.properties";
	public static String testDataFolder;
	public static String analysisOutputFolder;
	
	public static String pubmedFolder;
	public static String commonAffiliationsXmlFolder;
	public static String affiliationsXmlFolder;
	public static String emailXmlFolder;
	public static String departmentXmlFolder;
	public static String grantXmlFolder;

	public static String scopusFolder;
	
	public static void main(String[] args) {
		ReCiterEngineProperty.loadProperty();
		System.out.println(ReCiterEngineProperty.affiliationsXmlFolder);
	}
	
	public static void loadProperty() {
		InputStream inputStream = null;
		try {
			Properties properties = new Properties();
			inputStream = new FileInputStream(reCiterPropertyFile);
			properties.load(inputStream);
			testDataFolder = properties.getProperty("test_data_folder");
			analysisOutputFolder = properties.getProperty("analysis_output_folder");
			commonAffiliationsXmlFolder = properties.getProperty("pubmed_common_affiliations_xml_folder");
			pubmedFolder = properties.getProperty("pubmed_xml_folder");
			affiliationsXmlFolder = properties.getProperty("pubmed_affiliations_xml_folder");
			emailXmlFolder = properties.getProperty("pubmed_email_xml_folder");
			departmentXmlFolder = properties.getProperty("pubmed_dept_xml_folder");
			grantXmlFolder = properties.getProperty("pumbed_grant_xml_folder");
			scopusFolder = properties.getProperty("scopus_xml_folder");
		} catch (Exception e) {
			slf4jLogger.error("Error reading properties file", e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				slf4jLogger.error("Error closing inputStream while loading property file.", e);
			}
		}
	}
	
	public List<String> getCwids() {
		List<String> cwids = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(Paths.get(testDataFolder),Charset.defaultCharset())) {
			stream.forEach(e -> cwids.add(e));
		} catch (IOException e) {
			slf4jLogger.error("Error fetching list of cwids from disk.", e);
		}
		return cwids;
	}
}
