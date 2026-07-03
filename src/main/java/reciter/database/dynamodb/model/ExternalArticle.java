package reciter.database.dynamodb.model;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A publication manually added from a non-PubMed source (Scopus, Web of Science,
 * OpenAlex). External articles never enter feature generation, scoring, or
 * Analysis writes; they are appended to feature-generator API output at
 * serialization time when includeExternal=true is requested.
 */
@Data
@NoArgsConstructor
@DynamoDBTable(tableName = "ExternalArticle")
public class ExternalArticle {

    @DynamoDBHashKey
    private String uid;

    /**
     * Prefixed canonical identifier, e.g. "SCOPUS:85123456789",
     * "OPENALEX:W2741809807", "WOS:000123456700001".
     */
    @DynamoDBRangeKey
    private String articleId;

    private String doi;

    /** Present only when the source record carries a PMID (should normally be blocked at add time). */
    private Long pmid;

    private String title;
    private String journalOrVenue;
    private List<String> authors;

    /** Publication date as provided by the source, ideally ISO-8601 (yyyy or yyyy-MM-dd). */
    private String pubDate;

    private String publicationType;

    /** SCOPUS | WOS | OPENALEX — must agree with the articleId prefix. */
    private String sourceType;

    private String addedBy;

    /** ISO-8601 instant, set server-side at add time. */
    private String dateAdded;

    /** dropdown-search | scopus-authorships-tab */
    private String method;

    /** True once superseded by a PubMed record with the same DOI; excluded from API merge. */
    private Boolean suppressed;

    private Long supersededByPmid;

    /** Raw source API record as a JSON string, for provenance/debugging. */
    private String rawRecord;
}
