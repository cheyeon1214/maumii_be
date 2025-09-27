package com.project.maumii_be.service.user;

import com.project.maumii_be.repository.ChartReportRepository;
import com.project.maumii_be.repository.UserRepository;
import com.project.maumii_be.util.EmojiCalendarBuilder;
import com.project.maumii_be.util.EmojiFlowBuilder;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MonthlyEmotionReportService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final ChartReportRepository chartReportRepository;

    public MonthlyEmotionReportService(JavaMailSender mailSender,
                                       UserRepository userRepository, ChartReportRepository chartReportRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.chartReportRepository = chartReportRepository;
    }

    public void sendMonthlyReport(String toEmail, String uId,
                                  YearMonth ym, Map<LocalDate, String> emojiMap) throws Exception {

        // 아동 이름 조회
        String childName = userRepository.findNameByUId(uId)
                .orElse("이름없음");

        // 캘린더 HTML 생성 (제목에 이름 포함)
        String calendarHtml = EmojiCalendarBuilder.buildEmojiCalendarHtml(
                ym, emojiMap, Locale.KOREAN, childName
        );

        List<Object[]> emotionFlow = chartReportRepository.findEmotionGroupFlow(uId, ym.getYear(), ym.getMonth().getValue());
        String flowHtml = EmojiFlowBuilder.buildEmojiFlowHtml(
                emotionFlow
        );

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("마음이 감정 캘린더 리포트 - " + ym.getYear() + "년 " + ym.getMonthValue() + "월");
        helper.setText(calendarHtml + flowHtml, true); // HTML 본문
        mailSender.send(msg);
    }
}