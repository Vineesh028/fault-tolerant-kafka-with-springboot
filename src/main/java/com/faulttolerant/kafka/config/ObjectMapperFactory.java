package com.faulttolerant.kafka.config;


import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ObjectMapperFactory {
	
    public static ObjectMapper objectMapper;
    
    public static synchronized ObjectMapper getObjectMapperInstance() {
        if(objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }
}