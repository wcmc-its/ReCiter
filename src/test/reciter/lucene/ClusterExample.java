package test.reciter.lucene;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ClusterExample {

	public static void main(String[] args) {
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		 JsonObjectBuilder builder = factory.createObjectBuilder();
		 
		 builder
		     .add("nodes", factory.createArrayBuilder()
		         .add(factory.createObjectBuilder()
		             .add("pmid", 1)
		             .add("article_title", "Article title")
		             .add("journal_title", "")
		             .add("authors", factory.createArrayBuilder()
		            		 .add(factory.createObjectBuilder()
		            				 .add("first_name", "")
		            				 .add("last_name", "")
		            				 .add("affiliation", "")))
		            .add("keywords", factory.createArrayBuilder()
		            		.add(factory.createObjectBuilder()
		            				.add("keyword", "")))
		            .add("group", 1)));
		 
		 builder
		     .add("links", factory.createArrayBuilder()
		    		 .add(factory.createObjectBuilder()
		    				 .add("source", 1)
		    				 .add("target", 0)
		    				 .add("value", 10)));
		 JsonObject value = builder.build();
		 System.out.println(value);
	}
}
