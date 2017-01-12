package reciter.database.mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import reciter.engine.erroranalysis.Analysis;

@Document(collection = "analysis")
public class AnalysisMongo {
	
	@Id
	private String cwid;
	private Analysis analysis;
	
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public Analysis getAnalysis() {
		return analysis;
	}
	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
}
