package com.group2.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CloudNativeEngineeringProjectGroup2BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudNativeEngineeringProjectGroup2BackendApplication.class, args);
	}

}
