package main.reciter.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import main.reciter.model.author.TargetAuthor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class AnalysisCSVWriter {

	private final CSVFormat format;

	public AnalysisCSVWriter() {
		format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
	}

	/**
	 * Outputs the list of AnalysisObjects to a Python CSV format.
	 * @param list
	 * @throws IOException
	 */
	public void writePythonCSV(List<AnalysisObject> list) throws IOException {
		PrintWriter writer = new PrintWriter("csv_python_output.csv", "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format.withDelimiter(','));

		printer.printRecord(
				"Similarity Threshold", "Article ID", "Title", "Journal", "Authors", 
				"Affiliations", "Keywords",
				"Status of ReCiter's determination for this article with respect to reference standard");
		for (AnalysisObject analysisObject : list) {

			double similarityThreshold = analysisObject.getSimilarityMeasure();
			int articleId = analysisObject.getReCiterArticle().getArticleID();
			String title = analysisObject.getReCiterArticle().getArticleTitle().getTitle();
			String journal = analysisObject.getReCiterArticle().getJournal().getJournalTitle();
			String authors = analysisObject.getReCiterArticle().getArticleCoAuthors().toAuthorCSV();
			String affiliation = analysisObject.getReCiterArticle().getAffiliationConcatenated();
			String keywords = analysisObject.getReCiterArticle().getArticleKeywords().getCommaConcatForm();
			String status = analysisObject.getStatus();

			int trueArticle;
			if ("True Positive".equals(status) || "False Negative".equals(status)) {
				trueArticle = 1;
			} else {
				trueArticle = -1;
			}
			printer.printRecord(
					similarityThreshold,
					articleId,
					title,
					journal,
					authors,
					affiliation,
					keywords,
					trueArticle);
		}
		printer.close();
		writer.close();
	}

	/**
	 * Outputs the list of AnalysisObjects to a CSV file.
	 * @param list
	 * @throws IOException
	 */
	public void write(List<AnalysisObject> list, String cwid) throws IOException {
		PrintWriter writer = new PrintWriter("data/csv_output/" + cwid + ".csv", "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format.withDelimiter(','));

		printer.printRecord(
				"Similarity Threshold", "Article ID", "Title", "Journal", "Publication Year", "Authors", 
				"Affiliations", "Keywords", "Cluster to which the article was assigned", 
				"Number of articles in the selected cluster",
				"Cluster ultimately selected in Phase 2 matching",
				"Status of ReCiter's determination for this article with respect to reference standard",
				"affiliation similarity",
				"keyword similarity");

		for (AnalysisObject analysisObject : list) {

			double similarityThreshold = analysisObject.getSimilarityMeasure();
			int articleId = analysisObject.getReCiterArticle().getArticleID();
			String title = analysisObject.getReCiterArticle().getArticleTitle().getTitle();
			String journal = analysisObject.getReCiterArticle().getJournal().getJournalTitle();
			int publicationYear = analysisObject.getYearOfPublication();
			String authors = analysisObject.getReCiterArticle().getArticleCoAuthors().toAuthorCSV();
			String affiliation = analysisObject.getReCiterArticle().getAffiliationConcatenated();
			String keywords = analysisObject.getReCiterArticle().getArticleKeywords().getCommaConcatForm();
			int clusterId = analysisObject.getClusterId();
			int numArticles = analysisObject.getNumArticlesInCluster();
			boolean selected = analysisObject.isSelected();
			String status = analysisObject.getStatus();

			double affiliationSim = 0;
			double keywordSim = 0;
			if (TargetAuthor.getInstance().getMap().get(clusterId) != null) {
				affiliationSim = TargetAuthor.getInstance().getMap().get(clusterId).get(0).getScore();
				keywordSim = TargetAuthor.getInstance().getMap().get(clusterId).get(1).getScore();
			}
			String booleanStatus;
			if (selected) booleanStatus = "Yes"; else booleanStatus = "No";

			printer.printRecord(
					similarityThreshold,
					articleId,
					title,
					journal,
					publicationYear,
					authors,
					affiliation,
					keywords,
					clusterId,
					numArticles,
					booleanStatus,
					status,
					affiliationSim,
					keywordSim);
		}
		printer.close();
		writer.close();
	}
}
