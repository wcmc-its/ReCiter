package reciter.xml.retriever.pubmed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubMedPersistenceWorker implements Runnable {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(PubMedPersistenceWorker.class);

	private final String url;
	private final String commonDirectory;
	private final String uid;
	private final String xmlFileName;
	
	public PubMedPersistenceWorker(String url, String commonDirectory, String uid, String xmlFileName) {
		this.url = url;
		this.commonDirectory = commonDirectory;
		this.uid = uid;
		this.xmlFileName = xmlFileName;
	}
	
	@Override
	public void run() {
		try {
			slf4jLogger.info("persisting PubMed articles for uid=[" + uid + "].");
			persist(url, commonDirectory, uid, xmlFileName);
		} catch (IOException e) {
			slf4jLogger.error("Error persisting PubMed XML file for uid=[" + uid + "].", e);
		}
	}
	
	/**
	 * Save the url (XML) content in the {@code directoryLocation} with directory
	 * name {@code directoryName} and file name {@code fileName}.
	 * 
	 * @param url URL
	 * @param commonDirectory directory path.
	 * @param uid directory name.
	 * @param xmlFileName file name.
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws UnsupportedEncodingException 
	 */
	public void persist(String url, String commonDirectory, String uid, String xmlFileName) 
			throws UnsupportedEncodingException, MalformedURLException, IOException {

		File dir = new File(commonDirectory + uid);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
		String outputFileName = commonDirectory + uid + "/" + xmlFileName + ".xml";
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "UTF-8"));

		String inputLine;
		while ((inputLine = bufferedReader.readLine()) != null) {
			bufferedWriter.write(inputLine);
			bufferedWriter.newLine();
		}

		bufferedReader.close();
		bufferedWriter.close();

	}
}
