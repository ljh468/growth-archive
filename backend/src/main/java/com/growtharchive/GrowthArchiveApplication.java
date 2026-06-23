package com.growtharchive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GrowthArchiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrowthArchiveApplication.class, args);
    }
}
