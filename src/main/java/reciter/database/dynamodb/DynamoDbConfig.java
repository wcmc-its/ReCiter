package reciter.database.dynamodb;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

@Slf4j
@Configuration
//@EnableDynamoDBRepositories(basePackages = "reciter.database.dynamodb.repository", dynamoDBMapperConfigRef = "dynamoDBMapperConfig")
public class DynamoDbConfig {

	private String amazonDynamoDBEndpoint = System.getenv("AMAZON_DYNAMODB_ENDPOINT");

	private String amazonAWSAccessKey = System.getenv("AMAZON_AWS_ACCESS_KEY");

	private String amazonAWSSecretKey = System.getenv("AMAZON_AWS_SECRET_KEY");

	private String basePackage = "reciter.database.dynamodb.model";

	@Value("${aws.dynamoDb.local.port}")
	private String dynamoDbLocalPort;

	@Value("${aws.dynamoDb.local.region}")
	private String dynamodbLocalRegion;

	@Value("${aws.dynamoDb.local.accesskey}")
	private String dynamodbLocalAccessKey;

	@Value("${aws.dynamoDb.local.secretkey}")
	private String dynamodbLocalSecretKey;

	@Value("${aws.dynamoDb.local.dbpath}")
	private String dynamoDbPath;

	@Value("${aws.dynamoDb.local}")
	private boolean isDynamoDbLocal;

	@Value("${aws.dynamodb.settings.region}")
	private String dyanmodbRegion;

	@Value("${aws.dynamodb.settings.table.readcapacityunits}")
	private Long READ_CAPACITY_UNITS;

	@Value("${aws.dynamodb.settings.table.writecapacityunits}")
	private Long WRITE_CAPACITY_UNITS;

	@Value("${aws.dynamodb.settings.table.create}")
	private boolean createDynamoDbTable;

	@Value("${aws.dynamodb.settings.table.billingmode}")
	private BillingMode billingMode;
	
	
	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
		return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
	}
	
	
	
	
	@Bean
	public DynamoDbClient dynamoDbClient1() {
		DynamoDbClient dynamoDbClient = null;

		if (isDynamoDbLocal) {
			try {
				ProcessBuilder processBuilder = new ProcessBuilder("java", "-Djava.library.path=./native2-libs", "-jar",
						dynamoDbPath+"./DynamoDBLocal.jar",
						"-dbPath", dynamoDbPath);
				processBuilder.start();

				dynamoDbClient = DynamoDbClient.builder().httpClient(ApacheHttpClient.create())
						.endpointOverride(URI.create("http://localhost:" + dynamoDbLocalPort)).build();
				ListTablesResponse listTablesResponse = dynamoDbClient.listTables(ListTablesRequest.builder().build());
				System.out.println("tset");

			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}

		} else {
			if (StringUtils.isEmpty(dyanmodbRegion)) {
				throw new BeanCreationException(
						"The aws.dynamodb.settings.region is not set in application.propeties file. Please provide a valid  AWS region such as us-east-1 or eu-central-1. For list of valid regions see - https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html#Concepts.RegionsAndAvailabilityZones.Availability");
			}

			dynamoDbClient = DynamoDbClient.builder().region(Region.of(dyanmodbRegion))
					.credentialsProvider(DefaultCredentialsProvider.create()).build();

		}
		if (dynamoDbClient != null) {
			if (createDynamoDbTable) {
				log.info("Creating all required dynamodb tables for ReCiter");
			//	createTables(dynamoDbClient);
			} else {
				log.info("Skipping table creation for dynamoDB.");
				log.warn(
						"This might cause issues with your application if tables are not created. Please set the aws.dynamodb.settings.table.create in application.properties as true if not already created.");
			}
		} else {
			log.info("aws.dynamoDb.local needs to have a boolean value set in application.propeties");
		}
		return dynamoDbClient;
	}

	/**
	 * Creates the necessary tables.
	 */
	/*
	 * private void createTables(DynamoDbClient dynamoDbClient) { List existing
	 * tables in DynamoDB ListTablesRequest listTablesRequest =
	 * ListTablesRequest.builder().build(); ListTablesResponse listTablesResponse =
	 * dynamoDbClient.listTables(listTablesRequest);
	 * 
	 * Scan for classes with DynamoDBTable annotation
	 * ClassPathScanningCandidateComponentProvider scanner = new
	 * ClassPathScanningCandidateComponentProvider(false);
	 * 
	 * Reflections reflections = new Reflections(new ConfigurationBuilder()
	 * .setUrls(ClasspathHelper.forPackage(basePackage))
	 * .setScanners(Scanners.TypesAnnotated) .filterInputsBy(new
	 * FilterBuilder().includePackage(basePackage)) );
	 * 
	 * for (Class<?> clazz : reflections.getTypesAnnotatedWith(DynamoDbBean.class))
	 * { DynamoDbTableName tableAnnotation =
	 * clazz.getAnnotation(DynamoDbTableName.class); if (tableAnnotation == null) {
	 * log.error("Class " + clazz.getName() +
	 * " is missing @DynamoDbTableName annotation."); continue; }
	 * 
	 * 
	 * String tableName = tableAnnotation.value();
	 * 
	 * scanner.addIncludeFilter(new AnnotationTypeFilter(DynamoDbBean.class));
	 * 
	 * 
	 * 
	 * 
	 * if (!listTablesResponse.tableNames().contains(tableName)) { String keyName =
	 * null; for (Field field : clazz.getDeclaredFields()) { if
	 * (field.isAnnotationPresent(DynamoDbAttribute.class)) { keyName =
	 * field.getName(); break; } }
	 * 
	 * if (keyName == null) { throw new IllegalArgumentException("Class " +
	 * clazz.getName() + " lacks a valid DynamoDB partition key."); }
	 * 
	 * List<AttributeDefinition> attributeDefinitions = new
	 * ArrayList<AttributeDefinition>();
	 * if(tableName.equalsIgnoreCase("ScienceMetrixDepartmentCategory") ||
	 * tableName.equalsIgnoreCase("ScienceMetrix") ||
	 * tableName.equalsIgnoreCase("PubMedArticle")) {
	 * attributeDefinitions.add(AttributeDefinition.builder().attributeName(keyName)
	 * .attributeType(ScalarAttributeType.N).build());
	 * 
	 * } else {
	 * attributeDefinitions.add(AttributeDefinition.builder().attributeName(keyName)
	 * .attributeType(ScalarAttributeType.S).build()); }
	 * 
	 * 
	 * List<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement>();
	 * keySchemaElements.add(KeySchemaElement.builder().attributeName(keyName).
	 * keyType(KeyType.HASH).build());
	 * 
	 * CreateTableRequest createTableRequest = CreateTableRequest.builder()
	 * .tableName(tableName) .keySchema(keySchemaElements)
	 * .attributeDefinitions(attributeDefinitions) .build();
	 * 
	 * if(billingMode != null && billingMode == BillingMode.PAY_PER_REQUEST) {
	 * createTableRequest = createTableRequest.toBuilder()
	 * .billingMode(BillingMode.PAY_PER_REQUEST) .build(); } else if(billingMode !=
	 * null && billingMode == BillingMode.PROVISIONED) { ProvisionedThroughput
	 * provisionedThroughput = ProvisionedThroughput.builder()
	 * .readCapacityUnits(READ_CAPACITY_UNITS)
	 * .writeCapacityUnits(WRITE_CAPACITY_UNITS) .build(); createTableRequest =
	 * createTableRequest.toBuilder() .billingMode(BillingMode.PROVISIONED)
	 * .provisionedThroughput(provisionedThroughput) .build(); }
	 * 
	 * 
	 * if(createTableRequest != null) { CreateTableResponse createTableResponse =
	 * dynamoDbClient.createTable(createTableRequest); }
	 * log.info("Waiting for DynamoDB table " + tableName +
	 * " to be created in AWS."); try {
	 * dynamoDbClient.waiter().waitUntilTableExists(DescribeTableRequest.builder().
	 * tableName(tableName).build());
	 * 
	 * 
	 * } catch (DynamoDbException e) { log.error(e.getMessage()); }
	 * log.info("DynamoDB table " + tableName + " has been created in AWS."); } else
	 * { log.info("DynamoDB table " + tableName +
	 * " required for ReCiter is already created."); } }
	 * 
	 * }
	 * 
	 */

}