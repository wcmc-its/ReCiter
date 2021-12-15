package reciter.swagger;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("reciter.controller"))
                .paths(regex("/reciter.*"))
                .build()
                .apiInfo(apiInfo());
//                .securitySchemes(Arrays.asList(apiKey()));
    }
    
   private ApiInfo apiInfo() {
       return new ApiInfoBuilder().title("ReCiter publication management system")
               .description("Populate and retrieve identity data for a scholar. Trigger publication lookups and scoring for a scholar. Retrieve publications for a scholar. Interact with ReCiter Publication Manager. More info here: http://github.com/wcmc-its/ReCiter/").termsOfServiceUrl("")
               .contact(new Contact("Paul J. Albert", "https://github.com/wcmc-its/ReCiter", "paa2013@med.cornell.edu"))
               .contact(new Contact("Sarbajit Dutta", "https://github.com/wcmc-its/ReCiter", "szd2013@med.cornell.edu"))
               .license("Apache License Version 2.0")
               .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
               .version("2.1.1")
               .build();
   }

   /*
   private ApiKey apiKey() {
       return new ApiKey("reciter", "reciter", "header");
   }
   */
}
