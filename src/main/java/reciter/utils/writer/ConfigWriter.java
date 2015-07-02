package reciter.utils.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import database.dao.IdentityDao;
import database.model.Identity;

/**
 * Writes each cwid's properties file. The property file is used to define the person's
 * information which is leveraged in the target author to cluster matching.
 * 
 * @author jil3004
 *
 */
public class ConfigWriter {

	private final static String DIR = "src/main/resources/data/properties";

	public void writeConfig(String cwid) {
		IdentityDao identityDao = new IdentityDao();
		Identity identity = identityDao.getIdentityByCwid(cwid);
		String firstName = identity.getFirstName();
		String lastName = identity.getLastName();
		String middleName = identity.getMiddleName();
		String title = identity.getTitle();
		String primaryAffiliation = identity.getPrimaryAffiliation();
		String primaryDepartment = identity.getPrimaryDepartment();

		File dir = new File(DIR + "/" + cwid);
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			
			// Assuming that cwid, firstName, lastName are not null.
			BufferedWriter bufferedWriter = new BufferedWriter(
					  new OutputStreamWriter(
		              new FileOutputStream(DIR + "/" + cwid + "/" + cwid + ".properties"), "utf-8"));
			
			bufferedWriter.write("cwid=" + cwid);
			bufferedWriter.newLine();
			
			if ("null".equals(title)) {
				bufferedWriter.write("authorKeywords=");
				bufferedWriter.newLine();	
			} else {
				bufferedWriter.write("authorKeywords=" + title);
				bufferedWriter.newLine();
			}
			
			bufferedWriter.write("coAuthors=");
			bufferedWriter.newLine();
			bufferedWriter.write("similarityThreshold=0.3");
			bufferedWriter.newLine();
			bufferedWriter.write("firstName=" + firstName);
			bufferedWriter.newLine();

			if ("null".equals(middleName)) {
				bufferedWriter.write("middleName=");
				bufferedWriter.newLine();
			} else {
				bufferedWriter.write("middleName=" + middleName);
				bufferedWriter.newLine();
			}

			bufferedWriter.write("lastName=" + lastName);
			bufferedWriter.newLine();
			
			if ("null".equals(primaryAffiliation)) {
				bufferedWriter.write("authorAffiliation=");
				bufferedWriter.newLine();
			} else {
				bufferedWriter.write("authorAffiliation=" + primaryAffiliation);
				bufferedWriter.newLine();
			}

			if ("null".equals(primaryDepartment)) {
				bufferedWriter.write("authorDepartment=");
				bufferedWriter.newLine();
			} else {
				bufferedWriter.write("authorDepartment=" + primaryDepartment);
				bufferedWriter.newLine();
			}

			bufferedWriter.write(
					"performRetrievePublication=false\n" + 
					"performRetrieveScopusAffiliation=false\n" +
					"useStemming=false\n" +
					"cosineSimilarityType=max\n" +
					"titleSimilarityWeight=0\n" +
					"journalSimilarityWeight=0\n" +
					"keywordSimilarityWeight=0\n" +
					"authorSimilarityWeight=0");
			
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
