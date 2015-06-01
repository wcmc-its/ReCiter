package main.xml;

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

	private String directory; // location in workspace where XMLs are stored.
	private static final Logger slf4jLogger = LoggerFactory.getLogger(AbstractXmlFetcher.class);
	
	public AbstractXmlFetcher(String directory) {
		this.setDirectory(directory);
	}

	/**
	 * Save the url (XML) content in the location specified by {@code DIRECTORY/directoryName} with file name {@code fileName}.
	 * @param url URL where the XML is retrieved.
	 * @param fileName name of the file to be stored on disk.
	 * @param directoryName directory name.
	 * @throws IOException
	 */
	public void saveXml(String url, String directoryName, String fileName) {
		File dir = new File(getDirectory() + directoryName);
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			String outputFileName = getDirectory() + directoryName + "/" + fileName + ".xml";
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

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}
