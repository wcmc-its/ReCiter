package reciter.engine.analysis;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import reciter.model.article.ReCiterPublicationTypeScopus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamoDBDocument
public class ReCiterArticlePublicationType {
	
	private String publicationTypeCanonical;
	private List<String> publicationTypePubMed;
	private ReCiterPublicationTypeScopus publicationTypeScopus;
}
