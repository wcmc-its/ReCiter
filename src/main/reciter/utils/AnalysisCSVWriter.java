package main.reciter.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class AnalysisCSVWriter {

	private final CSVFormat format;
	
	public AnalysisCSVWriter() {
		format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
	}
	
	public void write(List<AnalysisObject> list) throws IOException {
		PrintWriter writer = new PrintWriter("csv_output.csv", "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format.withDelimiter(','));
		
		printer.printRecord(
				"Similarity Threshold", "Article ID", "Title", "Journal", "Authors", 
				"Affiliations", "Keywords", "Cluster to which the article was assigned", 
				"Number of articles in the selected cluster",
				"Cluster ultimately selected in Phase 2 matching",
				"Status of ReCiter's determination for this article with respect to reference standard");
		for (AnalysisObject analysisObject : list) {
			
			double similarityThreshold = analysisObject.getSimilarityMeasure();
			int articleId = analysisObject.getClusterId();
			String title = analysisObject.getReCiterArticle().getArticleTitle().getTitle();
			String journal = analysisObject.getReCiterArticle().getJournal().getJournalTitle();
			String authors = analysisObject.getReCiterArticle().getArticleCoAuthors().toAuthorCSV();
//			String affiliation = analysisObject.getReCiterArticle().getArticleCoAuthors().getAffiliationConcatFormWithComma();
			String keywords = analysisObject.getReCiterArticle().getArticleKeywords().getCommaConcatForm();
			int clusterId = analysisObject.getClusterId();
			int numArticles = analysisObject.getNumArticlesInCluster();
			boolean selected = analysisObject.isSelected();
			String status = analysisObject.getStatus();
			
			String booleanStatus;
			if (selected) booleanStatus = "Yes"; else booleanStatus = "No";
			
			printer.printRecord(
					similarityThreshold,
					articleId,
					title,
					journal,
					authors,
					"",
					keywords,
					clusterId,
					numArticles,
					booleanStatus,
					status);
		}
		printer.close();
		writer.close();
	}
//	
	public static void main(String[] args) throws FileNotFoundException, IOException {

		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		
		//CSV Write Example using CSVPrinter
		PrintWriter writer = new PrintWriter("the-file-name.csv", "UTF-8");
		
		
		CSVPrinter printer = new CSVPrinter(writer, format.withDelimiter(','));
		printer.printRecord("ID","Name","Role","Salary");
		printer.printRecord("1", "3", "3", "3");
		
		//close the printer
		printer.close();
		
		writer.close();
	}
}
