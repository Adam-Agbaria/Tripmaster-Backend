package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.app.repository")
@ComponentScan(basePackages = { "com.example.app.*" })
@EntityScan(basePackages = "com.example.app.model")
public class TripMaster3Application {

	public static void main(String[] args) {
		SpringApplication.run(TripMaster3Application.class, args);
	}

}
