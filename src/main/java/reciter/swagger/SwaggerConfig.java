package reciter.swagger;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * springdoc-openapi configuration (replaces the abandoned springfox in #634 Stage 2).
 * Preserves the former springfox {@code Docket} scope (controllers under
 * {@code reciter.controller}, paths under {@code /reciter/**}) and API info/contact/license.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi reciterApi() {
        return GroupedOpenApi.builder()
                .group("reciter")
                .packagesToScan("reciter.controller")
                .pathsToMatch("/reciter/**")
                .build();
    }

    @Bean
    public OpenAPI reciterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ReCiter publication management system")
                        .description("Populate and retrieve identity data for a scholar. Trigger publication lookups and scoring for a scholar. Retrieve publications for a scholar. Interact with ReCiter Publication Manager. More info here: http://github.com/wcmc-its/ReCiter/")
                        .version("2.1.3")
                        .contact(new Contact()
                                .name("Paul J. Albert")
                                .url("https://github.com/wcmc-its/ReCiter")
                                .email("paa2013@med.cornell.edu"))
                        .license(new License()
                                .name("Apache License Version 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
