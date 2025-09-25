package com.project.maumii_be.controller;

import com.project.maumii_be.dto.EmotionResponse;
import com.project.maumii_be.service.EmotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/healthz")
// 프런트 출처(포트 5050)만 열기. 필요하면 127.0.0.1도 추가
@CrossOrigin(
        origins = { "http://localhost:5050", "https://192.168.230.9:5173" },
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS }
)
public class HealthController {

    private final EmotionService emotionService;
    // 생성자 주입 방식
    @Autowired
    public HealthController(EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    // 최근 POST 응답 저장
    private Map<String, Object> lastResponse = Map.of(
            "ok", false,
            "body", "",
            "label", "",
            "score", 0.0
    );

    @GetMapping
    public Map<String, Object> healthGet() {
        return lastResponse;
    }

    @PostMapping
    public Map<String, Object> healthPost(@RequestBody Map<String, Object> body) {
        System.out.println("받은 텍스트: " + body);
        // 음성 텍스트
        String text = body.getOrDefault("content", "").toString();

        // FastAPI 호출
        EmotionResponse response = emotionService.analyzeText(text);

        // 최근 응답 저장
        lastResponse = Map.of("ok", true,
                    "body", text,
                    "label", response.getLabel(),
                    "score", response.getScore()
                );
        return lastResponse;
    }
}