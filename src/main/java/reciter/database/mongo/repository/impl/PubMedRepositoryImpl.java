package reciter.database.mongo.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.AggregationOutput;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import reciter.database.mongo.repository.PubMedRepositoryCustom;
import reciter.model.pubmed.PubMedArticle;

public class PubMedRepositoryImpl implements PubMedRepositoryCustom {

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Override
	public List<PubMedArticle> findMatchingAuthorsByLastName(String lastName) {

		DBObject dbObject = (DBObject) JSON.parse((
				"{$match: {'medlineCitation.article.authorList.initials': 'C'}},"
				+ "{$project: {'medlineCitation.article.authorList': {$filter: {"
				+ "input: '$medlineCitation.article.authorList', as: 'authorList', cond: {$eq: ['$$authorList.initials', 'C']}"
				+ "}},"
				+ "_id: 0"
				+ "}})"));

		List<DBObject> dbObjects = new ArrayList<DBObject>();
		dbObjects.add(dbObject);
		AggregationOutput results = mongoTemplate.getCollection("pubmedarticle").aggregate(dbObjects);
		for (DBObject result : results.results()) {
			System.out.println(result.get("medlineCitation"));
		}
		System.out.println(results);
		return null;
	}
	
	
	/**
	 * 		
		
//		return mongoTemplate.find(q, PubMedArticle.class);
//		List<Entity> entities = this.template.find(q, Entity.class);
		
		Aggregation aggregation = newAggregation(
				match(Criteria.where("medlineCitation.article.authorList.initials").is("C")),
				project("medlineCitation.article.authorList")
		);
		
		AggregationResults<PubMedArticle> aggregationResults = mongoTemplate.aggregate(aggregation, "pubmedarticle", PubMedArticle.class);
		return aggregationResults.getMappedResults();
	 */
	/**
	 * 
	 * DBObject dbObject = (DBObject) JSON.parse((
				"{$match: {'medlineCitation.article.authorList.initials': 'C'}},"
				+ "{$project: {'medlineCitation.article.authorList': {$filter: {"
				+ "input: '$medlineCitation.article.authorList', as: 'authorList', cond: {$eq: ['$$authorList.initials', 'C']}"
				+ "}},"
				+ "_id: 0"
				+ "}});"));

		List<DBObject> dbObjects = new ArrayList<DBObject>();
		dbObjects.add(dbObject);
		AggregationOutput results = mongoTemplate.getCollection("pubmedarticle").aggregate(dbObjects);
		for (DBObject result : results.results()) {
			System.out.println(result.get("medlineCitation"));
		}
		System.out.println(results);
		return null;
	 */
}
