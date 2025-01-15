package com.coursework.kinotinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class KinotinderApplication {
    private static final Logger logger = LoggerFactory.getLogger(KinotinderApplication.class);
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(KinotinderApplication.class, args);
        logger.info("Application started successfully");
    }
}
