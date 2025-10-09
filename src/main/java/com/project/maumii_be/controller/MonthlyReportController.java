package com.project.maumii_be.controller;

import com.project.maumii_be.domain.Protector;
import com.project.maumii_be.repository.ProtectorRepository;
import com.project.maumii_be.service.user.MonthlyEmotionReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/reports")
@Tag(name = "Report API", description = "보호자에게 전송하는 레포트 관련 API")
public class MonthlyReportController {

    private final MonthlyEmotionReportService reportService;
    private final ProtectorRepository protectorRepository;

    public MonthlyReportController(MonthlyEmotionReportService reportService,
                                   ProtectorRepository protectorRepository) {
        this.reportService = reportService;
        this.protectorRepository = protectorRepository;
    }

    /**
     * 예) POST /reports/send-monthly?uId=codus1214&ym=2025-09
     * - uId: 아동 사용자 아이디 (Users.u_id / Protector.u_id)
     * - ym : 보고서 대상 연-월 (yyyy-MM). 생략하면 지난달.
     */
    @PostMapping("/send-monthly")
    public Map<String, Object> sendMonthly(@RequestParam String uId,
                                           @RequestParam(required = false) String ym) throws Exception {

        // 대상 YearMonth 결정
        YearMonth yearMonth = (ym != null && !ym.isBlank())
                ? YearMonth.parse(ym)  // "2025-09"
                : YearMonth.now(ZoneId.of("Asia/Seoul")).minusMonths(1);

        // 보호자 이메일 조회
        List<Protector> protectors = protectorRepository.findByUser_uId(uId);
        List<String> emails = protectors.stream()
                .map(Protector::getPEmail)
                .toList();
        if (emails.isEmpty()) {
            return Map.of(
                    "uId", uId,
                    "ym", yearMonth.toString(),
                    "sent", 0,
                    "message", "보호자 이메일이 없습니다."
            );
        }

        // (임시) 랜덤 이모지 맵 생성 — 실제로는 DB 집계 결과로 대체
        Map<LocalDate, String> emojiMap = makeRandomEmojiMap(uId, yearMonth);

        // 보호자 모두에게 발송
        for (String email : emails) {
            reportService.sendMonthlyReport(email, uId, yearMonth, emojiMap);
        }

        return Map.of(
                "uId", uId,
                "ym", yearMonth.toString(),
                "recipients", emails,
                "sent", emails.size(),
                "status", "ok"
        );
    }

    // 임시 이모지 생성기: uId+ym 기반 시드로 재현성 보장
    private Map<LocalDate, String> makeRandomEmojiMap(String uId, YearMonth ym) {
        String[] pool = {"😊","😌","😢","😡","😱","😨","🤢"};
        // 동일 요청에 항상 동일 결과가 나오도록 시드 고정
        long seed = Objects.hash(uId, ym.getYear(), ym.getMonthValue());
        Random rnd = new Random(seed);

        Map<LocalDate, String> map = new HashMap<>();
//        for (int d = 1; d <= 10; d++) {
//            map.put(ym.atDay(d), pool[rnd.nextInt(pool.length)]);
//        }
        map.put(ym.atDay(1), pool[0]);
        map.put(ym.atDay(2), pool[2]);
        map.put(ym.atDay(3), pool[3]);
        map.put(ym.atDay(4), pool[0]);

        map.put(ym.atDay(6), pool[2]);
        map.put(ym.atDay(7), pool[0]);
        map.put(ym.atDay(8), pool[3]);
        map.put(ym.atDay(9), pool[3]);
        map.put(ym.atDay(10), pool[0]);
        return map;
    }
}