package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// This class is what starts the server and keeps it running
// Makes sure that the client side has access by changing
// the CorsRegistry

@SpringBootApplication
public class DemoApplication {

    // Starts the server and keeps it running
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    // Gives the live server access to this api
    // Does so by changing the CorsRegistry
	@Bean
	public WebMvcConfigurer corsConfigurer() {
    	return new WebMvcConfigurer() {
            @SuppressWarnings("null")
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://127.0.0.1:5501")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }   
        };
    }
}
