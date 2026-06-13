package com.sho8lanatech.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Sho8lanaTechApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(Sho8lanaTechApiApplication.class, args);
	}

}
