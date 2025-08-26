package com.project.maumii_be.controller;

import com.project.maumii_be.dto.EmotionResponse;
import com.project.maumii_be.dto.TextRequest;
import com.project.maumii_be.service.EmotionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emotion")
public class EmotionController {

    private final EmotionService emotionService;

    public EmotionController(EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    @PostMapping
    public EmotionResponse analyze(@RequestBody TextRequest request) {
        return emotionService.analyzeText(request.getText());
    }
}