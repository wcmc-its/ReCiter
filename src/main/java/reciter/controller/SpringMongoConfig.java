package reciter.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

@Configuration
@EnableMongoRepositories("reciter")
public class SpringMongoConfig extends AbstractMongoConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String host;
    
    @Value("${spring.data.mongodb.port}")
    private Integer port;
    
    @Value("${spring.data.mongodb.username}")
    private String username;
    
    @Value("${spring.data.mongodb.database}")
    private String database;
    
    @Value("${spring.data.mongodb.password}")
    private String password;
    
	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
	}

	@Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator());
    }
	
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
    
	@Override
	protected String getDatabaseName() {
		return database;
	}

	@Override
	@Bean
	public Mongo mongo() throws Exception {
		List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
		serverAddresses.add(new ServerAddress(host, port));
		List<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>();
		mongoCredentials.add(MongoCredential.createCredential(username,database, password.toCharArray()));
		return new MongoClient(serverAddresses, mongoCredentials);
	}

	/**
	 * http://jonathan-whywecanthavenicethings.blogspot.com/2011/11/wheres-my-exceptions-spring-data.html
	 */
	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		MongoTemplate mongoTemplate = new MongoTemplate(mongo(), getDatabaseName());
		mongoTemplate.setWriteConcern(new WriteConcern(1));
		return mongoTemplate;
	}    
}
