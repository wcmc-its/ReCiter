package reciter.database.dynamodb;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.exceptions.DynamoDBLocalServiceException;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.shared.access.sqlite.AmazonDynamoDBOfflineSQLiteJob;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.util.TableUtils.TableNeverTransitionedToStateException;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dyanmodb.files.ScienceMetrixDepartmentCategoryFileImport;

@Slf4j
@Configuration
@EnableDynamoDBRepositories
        (basePackages = "reciter.database.dynamodb.repository", dynamoDBMapperConfigRef = "dynamoDBMapperConfig")
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
    
    @Value("${aws.dynamodb.settings.table.readcapacityunits}")
    private Long READ_CAPACITY_UNITS;
    
    @Value("${aws.dynamodb.settings.table.writecapacityunits}")
    private Long WRITE_CAPACITY_UNITS;
    
    @Value("${aws.dynamodb.settings.table.create}")
    private boolean createDynamoDbTable;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
    	AmazonDynamoDB amazonDynamoDB = null;
    	
    	if(isDynamoDbLocal) {
    		log.info("Using dynamodb local with port - " + dynamoDbLocalPort + " and dbPath - " + dynamoDbPath);
    		DynamoDBProxyServer server = null;
    		
    		try {
    			server = ServerRunner.createServerFromCommandLineArgs(new String[]{
					"-dbPath", 	this.dynamoDbPath,"-port", this.dynamoDbLocalPort
				});
				server.start();
				
				amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
	        			new AwsClientBuilder.EndpointConfiguration("http://localhost:" + this.dynamoDbLocalPort, dynamodbLocalRegion))
	    				 .withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {
							
							@Override
							public String getAWSSecretKey() {
								return dynamodbLocalSecretKey;
							}
							
							@Override
							public String getAWSAccessKeyId() {
								return dynamodbLocalAccessKey;
							}
						}))
	        			.build();
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			} 
    		/*finally {
                if(server != null) {
                    try {
						server.stop();
					} catch (Exception e) {
						log.error(e.getLocalizedMessage());
					}
                }
                if(amazonDynamoDB != null) {
                	amazonDynamoDB.shutdown();
                }
            } */
    		
    		 
    	} else {
    		log.info("Using dynamodb AWS with endpoint - " + amazonDynamoDBEndpoint);
    		 amazonDynamoDB = new AmazonDynamoDBClient(amazonAWSCredentials());
    		if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
                amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
            }
    		
    	}
    	if(amazonDynamoDB != null) {
    		if(createDynamoDbTable) {
    			log.info("Creating all required dynamodb tables for ReCiter");
    			createTables(amazonDynamoDB);
    		} else {
    			log.info("Skipping table creation for dynamoDB.");
    			log.warn("This might cause issues with your application if tables are not created. Please set the aws.dynamodb.settings.table.create in application.properties as true if not already created.");
    		}
    	} else {
    		log.info("aws.dynamoDb.local needs to have a boolean value set in application.propeties");
    	}
        return amazonDynamoDB;
    }

    @Bean
    public AWSCredentials amazonAWSCredentials() {
    		if(isDynamoDbLocal 
    				&& 
    				dynamodbLocalSecretKey != null && !dynamodbLocalSecretKey.isEmpty()
    				&&
    				dynamodbLocalAccessKey != null && !dynamodbLocalAccessKey.isEmpty()) {
    			return new BasicAWSCredentials(dynamodbLocalAccessKey, dynamodbLocalSecretKey);
    		} else if(!isDynamoDbLocal && amazonAWSAccessKey != null && !amazonAWSAccessKey.isEmpty()
    				&&
    				amazonAWSSecretKey != null && !amazonAWSSecretKey.isEmpty()) {
    			return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    		} else {
    			return new BasicAWSCredentials(null, null);
    		}
    }
    
    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
    	DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
    	return builder.build();
    }
    
    /**
     * Creates the necessary tables.
     */
    private void createTables(AmazonDynamoDB amazonDynamoDB) {
    	
    	//Sometimes using Mac(OSX) there might be issues with sqllite being added to java library path. In that case copy the libsqlite4java jar to Extensions folder in /Library/Java/Extensions
    	//sudo cp ~/.m2/repository/com/almworks/sqlite4java/libsqlite4java-osx/1.0.392/libsqlite4java-osx-1.0.392.dylib /Library/Java/Extensions This will allow local dynamoDb to use sqllite for all purposes.
    	ListTablesResult listTablesResult = amazonDynamoDB.listTables();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(DynamoDBTable.class));

        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(bd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                // Never will happen. Do nothing
            }

            String tableName = clazz.getAnnotation(DynamoDBTable.class).tableName(); //String.format("%s-%s" , environment, clazz.getAnnotation(DynamoDBTable.class).tableName());

            if (!listTablesResult.getTableNames().contains(tableName)) {
                String keyName = null;
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(DynamoDBHashKey.class)) {
                        keyName = field.getName();
                        break;
                    }
                }

                if (keyName == null) {
                    throw new DynamoDBMappingException(String.format("The class %s has a not valid Dynamo Hash Key",
                                                                     bd.getBeanClassName()));
                }

                List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
                if(tableName.equalsIgnoreCase("ScienceMetrixDepartmentCategory") 
                		|| 
                		tableName.equalsIgnoreCase("ScienceMetrix") 
                		|| 
                		tableName.equalsIgnoreCase("PubMedArticle")) {
                	attributeDefinitions.add(new AttributeDefinition().withAttributeName(keyName).withAttributeType(ScalarAttributeType.N));
                	if(tableName.equalsIgnoreCase("ScienceMetrix")) {
                		attributeDefinitions.add(new AttributeDefinition().withAttributeName("issn").withAttributeType(ScalarAttributeType.S));
                		attributeDefinitions.add(new AttributeDefinition().withAttributeName("eissn").withAttributeType(ScalarAttributeType.S));
                	}
                } else {
                	attributeDefinitions.add(new AttributeDefinition().withAttributeName(keyName).withAttributeType(ScalarAttributeType.S));
                }
                
                if(tableName.equalsIgnoreCase("ScienceMetrix")) {
                	List<GlobalSecondaryIndex> globalSecondardyIndexes = new ArrayList<GlobalSecondaryIndex>();
                	
                	List<KeySchemaElement> keyTableSchemaElements = new ArrayList<KeySchemaElement>();
                	keyTableSchemaElements.add(new KeySchemaElement().withAttributeName(keyName).withKeyType(KeyType.HASH));
                	
                	GlobalSecondaryIndex issnIndex = new GlobalSecondaryIndex()
                			.withIndexName("issn-index")
                			.withKeySchema(new KeySchemaElement().withAttributeName("issn").withKeyType(KeyType.HASH))
                			.withProvisionedThroughput(new ProvisionedThroughput(READ_CAPACITY_UNITS, WRITE_CAPACITY_UNITS))
                			.withProjection(new Projection().withProjectionType(ProjectionType.ALL));
                	
                	GlobalSecondaryIndex eissnIndex = new GlobalSecondaryIndex()
                			.withIndexName("eissn-index")
                			.withKeySchema(new KeySchemaElement().withAttributeName("eissn").withKeyType(KeyType.HASH))
                			.withProvisionedThroughput(new ProvisionedThroughput(READ_CAPACITY_UNITS, WRITE_CAPACITY_UNITS))
                			.withProjection(new Projection().withProjectionType(ProjectionType.ALL));
                	
                	
                    
                    globalSecondardyIndexes.add(issnIndex);
                    globalSecondardyIndexes.add(eissnIndex);
                    
                    CreateTableRequest request =
                            new CreateTableRequest()
                                                    .withTableName(tableName)
                                                    .withAttributeDefinitions(attributeDefinitions)
                                                    .withKeySchema(keyTableSchemaElements)
                                                    .withGlobalSecondaryIndexes(globalSecondardyIndexes)
                                                    .withProvisionedThroughput(
                                                                               new ProvisionedThroughput().withReadCapacityUnits(READ_CAPACITY_UNITS)
                                                                                                          .withWriteCapacityUnits(WRITE_CAPACITY_UNITS));
                    
                    amazonDynamoDB.createTable(request);
                	log.info("Waiting for table " + tableName + " to be created in AWS.");
                	try {
						TableUtils.waitUntilActive(amazonDynamoDB, tableName);
					} catch (TableNeverTransitionedToStateException | InterruptedException e) {
						log.error(e.getMessage());
					}
                } else {
                	List<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement>();
	                keySchemaElements.add(new KeySchemaElement().withAttributeName(keyName).withKeyType(KeyType.HASH));
	
	                CreateTableRequest request =
	                        new CreateTableRequest()
	                                                .withTableName(tableName)
	                                                .withKeySchema(keySchemaElements)
	                                                .withAttributeDefinitions(attributeDefinitions)
	                                                .withProvisionedThroughput(
	                                                                           new ProvisionedThroughput().withReadCapacityUnits(READ_CAPACITY_UNITS)
	                                                                                                      .withWriteCapacityUnits(WRITE_CAPACITY_UNITS));
	                amazonDynamoDB.createTable(request);
	                log.info("Waiting for table " + tableName + " to be created in AWS.");
	                try {
						TableUtils.waitUntilActive(amazonDynamoDB, tableName);
					} catch (TableNeverTransitionedToStateException | InterruptedException e) {
						log.error(e.getMessage());
					}
                }
                
            }

        }
    	
    }
    
    
}