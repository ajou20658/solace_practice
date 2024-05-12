package com.example.solacetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolaceTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolaceTestApplication.class, args);
	}

}
