package com.example.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@Configuration
@EnableJpaRepositories(basePackages = "com.example.app.repository", entityManagerFactoryRef = "entityManagerFactory")
@EntityScan("com.example.app.model") 
public class JpaRepositoryConfig {
}
