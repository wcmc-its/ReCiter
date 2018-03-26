package reciter.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        ParameterBuilder parameterBuilder = new ParameterBuilder();
        List<Parameter> parameterBuilders = new ArrayList<>(1);
        parameterBuilder
                .name("header")
                .description("Description of header")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(true);
        parameterBuilders.add(parameterBuilder.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(parameterBuilders)
                .select()
                .apis(RequestHandlerSelectors.basePackage("reciter.controller"))
                .paths(regex("/reciter.*"))
                .build();
//                .apiInfo(apiInfo())
//                .securitySchemes(Arrays.asList(apiKey()));
    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder().title("ReCiter API")
//                .description("The REST API for ReCiter.").termsOfServiceUrl("")
//                .contact(new Contact("Jie Lin", "", ""))
//                .license("Apache License Version 2.0")
//                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
//                .version("0.0.1")
//                .build();
//    }
//
//    private ApiKey apiKey() {
//        return new ApiKey("reciter", "reciter", "header");
//    }
}
