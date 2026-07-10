package com.project.ScanScoreJava;

import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScanScoreJavaApplication {

	public static void main(String[] args) {
		OpenCV.loadLocally();

		SpringApplication.run(ScanScoreJavaApplication.class, args);
	}

}
