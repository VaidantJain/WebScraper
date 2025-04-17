package com.example.webScraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.webScraper.repository")
public class WebScraperApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebScraperApplication.class, args);
	}

}
