package reciter.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration 
@ComponentScan(basePackages = "reciter")
@EnableWebMvc   
public class AppConfig {  
} 
