package reciter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAutoConfiguration
@EnableAsync
@EnableMongoRepositories("reciter.database.mongo")
@ComponentScan("reciter")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}