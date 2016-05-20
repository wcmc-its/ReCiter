package reciter.xml.retriever.pubmed.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class EsearchObjectJsonDeserializer implements JsonDeserializer<EsearchObject> {

	@Override
	public EsearchObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) 
			throws JsonParseException {
		
		final JsonObject jsonObject = jsonElement.getAsJsonObject();
		EsearchResult eSearchResults = context.deserialize(jsonObject.get("esearchresult"), EsearchResult.class);
		final EsearchObject eSearchObject = new EsearchObject();
		
		if (eSearchResults != null) {
			eSearchObject.seteSearchResult(eSearchResults);
		}
		
		return eSearchObject;
	}

}
