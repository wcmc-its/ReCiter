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

	private String directory; // location in workspace where XMLs are stored.
	private boolean performRetrievePublication = false;

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
		File dir = new File(getDirectory() + directoryName);
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new URL(url).openStream(), "UTF-8")); // Github issue: https://github.com/wcmc-its/ReCiter/issues/87
			String outputFileName = getDirectory() + directoryName + "/"
					+ fileName + ".xml";
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(outputFileName), "UTF-8"));
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				bufferedWriter.write(removeSpecialStrings(inputLine)); // Github issue: https://github.com/wcmc-its/ReCiter/issues/87
				bufferedWriter.newLine();
			}
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException e) {
			slf4jLogger.warn(e.getMessage());
		}
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

	public boolean isPerformRetrievePublication() {
		return performRetrievePublication;
	}

	public void setPerformRetrievePublication(boolean performRetrievePublication) {
		this.performRetrievePublication = performRetrievePublication;
	}

	/**
	 * Github issue: https://github.com/wcmc-its/ReCiter/issues/87
	 * 
	 * @param lineString
	 * @return
	 * 
	 */
	public static String removeSpecialStrings( String lineString){
		if (lineString.contains(" & ")) {
			return lineString.replaceAll(" & ", " &amp; ");
		} else {
			return lineString;
		}
//		  if (lineString.contains("&quot")) {
//		      return lineString.replaceAll("&quot", ""); 
//		  } else if (lineString.contains("&amp;")) {
//			  return lineString.replaceAll("amp;", ""); 
//		  } else if (lineString.contains("?><")) {
//			 return lineString;
//		  } else {
//			  return lineString;
//		  }
	}
}
