package reciter.xml.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractXmlFetcher implements XmlFetcher {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(AbstractXmlFetcher.class);
	
	/**
	 * Save the url (XML) content in the {@code directoryLocation} with directory
	 * name {@code directoryName} and file name {@code fileName}.
	 * 
	 * @param url URL
	 * @param commonDirectory directory path.
	 * @param cwid directory name.
	 * @param xmlFileName file name.
	 */
	public void saveXml(String url, String commonDirectory, String cwid, String xmlFileName) {
		
		slf4jLogger.info("commonDirectory=[" + commonDirectory + "].");
		
		File dir = new File(commonDirectory + cwid);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
			String outputFileName = commonDirectory + cwid + "/" + xmlFileName + ".xml";
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "UTF-8"));
			
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				bufferedWriter.write(inputLine);
				bufferedWriter.newLine();
			}
			
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException e) {
			slf4jLogger.warn(e.getMessage());
		}
	}
}
