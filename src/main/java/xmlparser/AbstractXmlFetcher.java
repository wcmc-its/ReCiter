package xmlparser;

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
	 * Location where fetched XMLs are stored.
	 */
	protected String directory; 
	
	/**
	 * Save the url (XML) content in the {@code directoryLocation} with directory
	 * name {@code directoryName} and file name {@code fileName}.
	 * 
	 * @param url URL
	 * @param directoryLocation directory path.
	 * @param directoryName directory name.
	 * @param fileName file name.
	 */
	public void saveXml(String url, String directoryLocation, String directoryName, String fileName) {
		File dir = new File(getDirectory() + directoryName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
			String outputFileName = directoryLocation + directoryName + "/" + fileName + ".xml";
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
	
	/**
	 * Save the url (XML) content in the location specified by
	 * {@code DIRECTORY/directoryName} with file name {@code fileName}.
	 * 
	 * @param url
	 *            URL where the XML is retrieved.
	 * @param fileName
	 *            name of the file to be stored on disk.
	 * @param directoryName
	 *            directory name.
	 * @throws IOException
	 */
	public void saveXml(String url, String directoryName, String fileName) {
		saveXml(url, getDirectory(), directoryName, fileName);
	}

	public AbstractXmlFetcher() {}
	
	public AbstractXmlFetcher(String directory) {
		this.setDirectory(directory);
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
}
