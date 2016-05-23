package reciter.xml.retriever.pubmed.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class EsearchResultJsonDeserializer implements JsonDeserializer<EsearchResult> {

	@Override
	public EsearchResult deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		
		final JsonObject jsonObject = jsonElement.getAsJsonObject();
		String count = jsonObject.get("count").getAsString();
		String[] idList = context.deserialize(jsonObject.get("idlist"), String[].class);
		
		final EsearchResult eSearchResult = new EsearchResult();
		eSearchResult.setCount(count);
		eSearchResult.setIdList(idList);
		return eSearchResult;
	}

}
