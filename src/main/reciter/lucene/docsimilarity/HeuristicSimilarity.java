package main.reciter.lucene.docsimilarity;

import main.reciter.model.affiliation.DepartmentExtractImpl;
import main.reciter.model.affiliation.DepartmentExtractor;
import main.reciter.model.article.ReCiterArticle;

public class HeuristicSimilarity extends AbstractCosineSimilarity {

	@Override
	public double documentSimilarity(ReCiterArticle docA, ReCiterArticle docB) {
		
		double score = 0;
		
		DepartmentExtractor deptExtractor = new DepartmentExtractImpl();
		String departmentA = deptExtractor.extractDepartment(docA);
		String departmentB = deptExtractor.extractDepartment(docB);
		
		if (departmentA.equals(departmentB)) {
			return 1;
		} else if (departmentA.contains(departmentB) || departmentB.contains(departmentA)) {
			return 1;
		}
		
		MaxCosineSimilarity max = new MaxCosineSimilarity();
		return max.documentSimilarity(docA, docB);
	}

}
