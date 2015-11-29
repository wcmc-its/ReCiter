package reciter.csv;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import reciter.model.article.ReCiterArticle;

public class CSVWriter {

	private final CSVFormat format;
	private final String fileName;
	
	public CSVWriter(String fileName) {
		this.fileName = fileName;
		format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
	}
	
	public void write(List<ReCiterArticle> reCiterArticles) throws IOException {
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format);
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			printer.print(reCiterArticle.getArticleId());
			printer.print(reCiterArticle.getAffiliationScore());
			
			printer.print(reCiterArticle.getNameStrategyScore());
			printer.print(reCiterArticle.getBoardCertificationStrategyScore());
			printer.print(reCiterArticle.getInternshipAndResidenceStrategyScore());
			printer.print(reCiterArticle.getKnownCoinvestigatorScore());
			printer.print(reCiterArticle.getEmailStrategyScore());
			printer.print(reCiterArticle.getDepartmentStrategyScore());
			printer.print(reCiterArticle.getAffiliationScore());
			printer.print(reCiterArticle.getScopusStrategyScore());
			printer.print(reCiterArticle.getCitizenshipStrategyScore());
			
			printer.print(reCiterArticle.getGoldStandard());
			printer.println();
		}
		printer.close();
		writer.close();
	}
}
