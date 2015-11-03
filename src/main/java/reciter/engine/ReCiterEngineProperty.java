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
	public String testDataCwidListFile;
	public String csvOutputFile;

	public ReCiterEngineProperty() {
		InputStream inputStream = null;
		try {
			Properties properties = new Properties();
			inputStream = new FileInputStream(reCiterPropertyFile);
			properties.load(inputStream);
			testDataCwidListFile = properties.getProperty("test.data.cwid.list.file");
			csvOutputFile = properties.getProperty("csv.output.file");

		} catch (Exception e) {
			slf4jLogger.error(e.getMessage());
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				slf4jLogger.error("Error closing inputStream while loading property file.");
			}
		}
	}
	
	public List<String> getCwids() {
		List<String> cwids = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(Paths.get(testDataCwidListFile),Charset.defaultCharset())) {
			stream.forEach(e -> cwids.add(e));
		} catch (IOException ex) {
			slf4jLogger.error(ex.getMessage());
		}
		return cwids;
	}
}
