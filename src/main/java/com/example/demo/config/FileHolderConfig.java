package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.domain.FileHolder;

// Configurates a fileHolder so that it can be injected
// Into the service layer so that it can be used

@Configuration
public class FileHolderConfig {
    
    @Bean
    public FileHolder fileHolder(){
        return new FileHolder();
    }

}
