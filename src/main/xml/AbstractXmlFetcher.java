package main.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

public abstract class AbstractXmlFetcher implements XmlFetcher {

	private String directory; // location in workspace where XMLs are stored.

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
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getDirectory() + directoryName + "/" + fileName + ".xml"), "UTF-8"));
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				bufferedWriter.write(inputLine);
				bufferedWriter.newLine();
			}
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}
