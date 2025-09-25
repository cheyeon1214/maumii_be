package com.project.maumii_be.service;

import com.project.maumii_be.dto.EmotionResponse;
import com.project.maumii_be.dto.TextRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmotionService {

    private final RestTemplate restTemplate;

    public EmotionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public EmotionResponse analyzeText(String text) {
        TextRequest request = new TextRequest();
        request.setText(text);

        // POST 로 /analyze 호출
        return restTemplate.postForObject("http://ai:8000/analyze", request, EmotionResponse.class);
    }
}