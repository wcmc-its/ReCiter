package reciter.algorithm.cluster;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.erroranalysis.Analysis;
import reciter.erroranalysis.AnalysisCSVWriter;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.model.ReCiterCWIDData;
import xmlparser.pubmed.PubmedXmlFetcher;

public class ReCiterExample {

	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);

	public static double totalPrecision = 0;
	public static double totalRecall = 0;
	public static int numCwids = 0;

	public static void main(String[] args) throws IOException {

		// Keep track of execution time of ReCiter .
		long startTime = System.currentTimeMillis();

		Files.walk(Paths.get(PubmedXmlFetcher.getDefaultLocation()))
				.forEach(
						filePath -> {
							if (Files.isRegularFile(filePath)) {
								String cwid = filePath.getFileName().toString()
										.replace("_0.xml", "");
								ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
								try {
									reCiterConfigProperty
											.loadProperty(ReCiterConfigProperty
													.getDefaultLocation()
													+ cwid
													+ "/"
													+ cwid
													+ ".properties");
								} catch (Exception e) {
									e.printStackTrace();
								}
								runExample(reCiterConfigProperty);
								numCwids++;
							}
						});

		slf4jLogger.info("Number of cwids: " + numCwids);
		slf4jLogger.info("Average Precision: " + totalPrecision / numCwids);
		slf4jLogger.info("Average Recall: " + totalRecall / numCwids);

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");

		/* Output the ReCiter performance summary as .csv file #75 */
		CSVFormat format;
		String CSV_OUTPUT = "src/main/resources/data/csv_output/";
		format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		PrintWriter writer = new PrintWriter(CSV_OUTPUT + ".csv", "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format);

		String header = "Count 		CWID	Precision	Recall		Average of precision and recall";

		printer.print(numCwids);
		printer.print(totalPrecision);
		printer.print(totalRecall);

		printer.print(header);

		String summary = "Overall precision" + totalPrecision + "\n"
				+ "Overall recall" + totalRecall + "\n" + "Overall average"
				+ (totalRecall / numCwids);

		printer.print(summary);
		printer.close();
		writer.close();
	}

	/**
	 * Setup the data to run the ReCiter algorithm.
	 * 
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 */
	public static void runExample(ReCiterConfigProperty reCiterConfigProperty) {
		ReCiterCWIDData data = new ReCiterCWIDData(reCiterConfigProperty); 
		ReCiterClusterer reCiterClusterer = new ReCiterClusterer(data.getCwid());
		Analysis analysis = reCiterClusterer.cluster(data.getFilteredArticleList());
		slf4jLogger.info(reCiterClusterer.getClusterInfo());
		slf4jLogger.info("Precision=" + analysis.getPrecision());
		totalPrecision += analysis.getPrecision();
		slf4jLogger.info("Recall=" + analysis.getRecall());
		totalRecall += analysis.getRecall();
		slf4jLogger.info("False Positive List: " + analysis.getFalsePositiveList());
		slf4jLogger.info("\n");
		// Write analysis to CSV.
		AnalysisCSVWriter analysisCSVWriter = new AnalysisCSVWriter();
		try {
			analysisCSVWriter.write(
					analysis.getAnalysisObjectList(), data.getCwid(), analysis.getPrecision(), analysis.getRecall());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}