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

	public String reCiterPropertyFile = "src/main/resources/config/reciter.properties";
	public String testDataFolder;
	public String analysisOutputFolder;

	public ReCiterEngineProperty() {
		InputStream inputStream = null;
		try {
			Properties properties = new Properties();
			inputStream = new FileInputStream(reCiterPropertyFile);
			properties.load(inputStream);
			testDataFolder = properties.getProperty("test_data_folder");
			analysisOutputFolder = properties.getProperty("analysis_output_folder");

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
