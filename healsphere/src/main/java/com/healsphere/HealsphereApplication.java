package com.healsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point: Spring Boot starts the embedded web server from here. */
@SpringBootApplication
public class HealsphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealsphereApplication.class, args);
    }
}
