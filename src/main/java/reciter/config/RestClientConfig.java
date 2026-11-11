package reciter.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

	/**
	 * Shared, thread-safe RestTemplate for the PubMed and Scopus retrieval services,
	 * built from Spring Boot's RestTemplateBuilder so Boot's configured customizations apply.
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
}
