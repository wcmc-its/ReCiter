package reciter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

	/**
	 * Shared, thread-safe RestTemplate for the PubMed and Scopus retrieval services.
	 * A plain RestTemplate keeps the same message converters as before the migration.
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
