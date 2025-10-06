package com.project.maumii_be.service.record;

import com.project.maumii_be.dto.bubble.EmotionResponse;
import com.project.maumii_be.dto.bubble.TextRequest;
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
        return restTemplate.postForObject("https://maumii-ai-43895739287.europe-west1.run.app/analyze", request, EmotionResponse.class);
    }
}