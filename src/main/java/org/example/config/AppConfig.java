package org.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.example")
public class AppConfig {
    // Spring Boot auto-configuration will handle most settings
    // This class ensures component scanning for our packages
}