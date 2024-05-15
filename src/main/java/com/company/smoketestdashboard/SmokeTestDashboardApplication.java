package com.company.smoketestdashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Sounak Paul
 */
@SpringBootApplication
@EnableScheduling
public class SmokeTestDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmokeTestDashboardApplication.class, args);
	}

}
