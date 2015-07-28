package reciter.erroranalysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class AnalysisCSVWriter {

	private final CSVFormat format;
	private static final String CSV_OUTPUT = "src/main/resources/data/csv_output/";
	
	public AnalysisCSVWriter() {
		format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
	}

	/**
	 * Outputs the list of AnalysisObjects to a CSV file.
	 * @param list
	 * @throws IOException
	 */
	public void write(List<AnalysisObject> list, String fileName) throws IOException {
		PrintWriter writer = new PrintWriter(CSV_OUTPUT + fileName + ".csv", "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format);
		
		// Print Header.
		for (String header : AnalysisConstants.getConstantsAsArray()) {
			printer.print(header);
		}
		printer.println();
		
		// Print each row of data.
		for (AnalysisObject analysisObject : list) {
			printer.print(analysisObject.getStatus().name());
			printer.print(analysisObject.getCwid());
			printer.print(analysisObject.getTargetName());
			printer.print(analysisObject.getPubmedSearchQuery());
			printer.print(analysisObject.getPmid());
			printer.print(analysisObject.getArticleTitle());
			printer.print(analysisObject.getFullJournalTitle());
			printer.print(analysisObject.getPublicationYear());
			printer.print(analysisObject.getScopusTargetAuthorAffiliation());
			printer.print(analysisObject.getScopusCoAuthorAffiliation());
			printer.print(analysisObject.getPubmedTargetAuthorAffiliation());
			printer.print(analysisObject.getPubmedCoAuthorAffiliation());
			printer.print(analysisObject.getArticleKeywords());
			printer.print(analysisObject.getNameMatchingScore());
			printer.print(analysisObject.isClusterOriginator());
			printer.print(analysisObject.getJournalSimilarityPhaseOne());
			printer.print(analysisObject.getCoauthorAffiliationScore());
			printer.print(analysisObject.getTargetAuthorAffiliationScore());
			printer.print(analysisObject.getKnownCoinvestigatorScore());
			printer.print(analysisObject.getFundingStatementScore());
			printer.print(analysisObject.getTerminalDegreeScore());
			printer.print(analysisObject.getDefaultDepartmentJournalSimilarityScore());
			printer.print(analysisObject.getDepartmentOfAffiliationScore());
			printer.print(analysisObject.getKeywordMatchingScore());
			printer.print(analysisObject.getPhaseTwoSimilarityThreshold());
			printer.print(analysisObject.getClusterArticleAssignedTo());
			printer.print(analysisObject.getCountArticlesInAssignedCluster());
			printer.print(analysisObject.isClusterSelectedInPhaseTwoMatching());
			printer.print(analysisObject.getAffiliationSimilarity());
			printer.print(analysisObject.getKeywordSimilarity());
			printer.print(analysisObject.getJournalSimilarityPhaseTwo());
			printer.println();
		}
		printer.close();
		writer.close();
	}
}
