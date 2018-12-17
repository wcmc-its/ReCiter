package reciter.model.article;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamoDBDocument
public class ReCiterPublicationTypeScopus {
	
	private String publicationTypeScopusAbbreviation;
	private String publicationTypeScopusLabel;
	
}
