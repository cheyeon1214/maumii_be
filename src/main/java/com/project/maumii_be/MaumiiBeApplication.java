package com.project.maumii_be;

import com.project.maumii_be.service.record.EmotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MaumiiBeApplication implements CommandLineRunner {

	@Autowired
	private EmotionService emotionService;

	public static void main(String[] args) {
		SpringApplication.run(MaumiiBeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
//		// FastAPI 테스트
//		String text = "오늘 기분이 너무 좋아!";
//		EmotionResponse response = emotionService.analyzeText(text);
//
//		System.out.println("=== FastAPI 테스트 결과 ===");
//		System.out.println("Label: " + response.getLabel());
//		System.out.println("Score: " + response.getScore());
	}
}
