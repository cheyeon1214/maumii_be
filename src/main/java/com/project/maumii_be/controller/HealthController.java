package com.project.maumii_be.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/healthz")
// 프런트 출처(포트 5050)만 열기. 필요하면 127.0.0.1도 추가
@CrossOrigin(
        origins = { "http://localhost:5050", "http://127.0.0.1:5050" },
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS }
)
public class HealthController {

    @GetMapping
    public Map<String, Object> healthGet() {
        return Map.of("ok", true);
    }

    @PostMapping
    public Map<String, Object> healthPost(@RequestBody Map<String, Object> body) {
        System.out.println("받은 텍스트: " + body);
        return Map.of("ok", true, "echo", body);
    }
}