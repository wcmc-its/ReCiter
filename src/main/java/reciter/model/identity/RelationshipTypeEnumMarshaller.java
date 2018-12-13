package reciter.model.identity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

import reciter.model.identity.KnownRelationship.RelationshipType;

public class RelationshipTypeEnumMarshaller implements DynamoDBMarshaller<RelationshipType> {

	@Override
	public String marshall(RelationshipType getterReturnResult) {
		return getterReturnResult.toString();
	}

	@Override
	public RelationshipType unmarshall(Class<RelationshipType> clazz, String obj) {
		return KnownRelationship.getEnum(obj);
	}
	
}
