package com.project.maumii_be.util;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class EmojiCalendarBuilder {

    public static String buildEmojiCalendarHtml(YearMonth ym,
                                                Map<LocalDate, String> emojiMap,
                                                Locale locale,
                                                String childName) {
        String[] days = {"일","월","화","수","목","금","토"};

        LocalDate firstDay = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();
        int startOffset = firstDay.getDayOfWeek().getValue() % 7; // SUN=0

        // ===== 인라인 스타일 =====
        String wrap = "max-width:680px;margin:0 auto;padding:8px 12px;"
                + "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Noto Sans KR',sans-serif;color:#111;";
        String h2 = "margin:12px 2px 10px;font-size:24px;line-height:1.25;";
        String table = "border-collapse:collapse;width:100%;table-layout:fixed;";
        String th = "padding:8px 0;font-size:12px;color:#666;border-bottom:1px solid #ececec;";
        String tdBase = "height:68px;vertical-align:top;border:1px solid #ececec;background:#fff;";
        String tdSun = "background:#fff7f7;"; // 일
        String tdSat = "background:#f7fbff;"; // 토
        String tdEmpty = "background:#fcfcfc;color:#bbb;";
        String cell = "position:relative;width:100%;height:100%;";
        String date = "position:absolute;top:6px;right:8px;font-size:11px;color:#888;";
        String emojiCss = "display:flex;align-items:center;justify-content:center;height:100%;font-size:22px;line-height:1;";

        // thead
        StringBuilder thead = new StringBuilder("<tr>");
        for (String d : days) thead.append("<th style='").append(th).append("'>").append(d).append("</th>");
        thead.append("</tr>");

        // tbody
        StringBuilder tbody = new StringBuilder();
        int cellIdx = 0;
        int totalCells = ((startOffset + daysInMonth + 6) / 7) * 7;

        for (int r = 0; r < totalCells / 7; r++) {
            tbody.append("<tr>");
            for (int c = 0; c < 7; c++, cellIdx++) {
                boolean before = cellIdx < startOffset;
                boolean after  = cellIdx >= startOffset + daysInMonth;

                StringBuilder tdStyle = new StringBuilder(tdBase);
                if (c == 0) tdStyle.append(tdSun);          // 일요일
                if (c == 6) tdStyle.append(tdSat);          // 토요일
                if (before || after) tdStyle.append(tdEmpty); // 앞/뒤 빈칸

                if (before || after) {
                    tbody.append("<td style='").append(tdStyle).append("'></td>");
                } else {
                    int dayNum = cellIdx - startOffset + 1;
                    LocalDate dateKey = ym.atDay(dayNum);
                    String emo = escapeHtml(emojiMap.getOrDefault(dateKey, "•"));

                    tbody.append("<td style='").append(tdStyle).append("'>")
                            .append("<div style='").append(cell).append("'>")
                            .append("<div style='").append(date).append("'>").append(dayNum).append("</div>")
                            .append("<div style='").append(emojiCss).append("'>").append(emo).append("</div>")
                            .append("</div>")
                            .append("</td>");
                }
            }
            tbody.append("</tr>");
        }

        // HTML
        return new StringBuilder()
                .append("<html><head><meta charset='UTF-8'></head><script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>" +
                        "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css\"><body>")
                .append("<div style='").append(wrap).append("'>")
                .append("<h2 style='").append(h2).append("'>")
                .append(escapeHtml(childName)).append("의 감정 캘린더 – ")
                .append(ym.getYear()).append("년 ")
                .append(ym.getMonth().getDisplayName(TextStyle.FULL, locale)).append("</h2>")
                .append("<table role='presentation' cellspacing='0' cellpadding='0' border='0' style='").append(table).append("'>")
                .append("<thead>").append(thead).append("</thead>")
                .append("<tbody>").append(tbody).append("</tbody>")
                .append("</table>")
                .append("<div style='margin:10px 2px 4px;font-size:12px;color:#666'>• 데이터가 없는 날은 점(•)으로 표시됩니다.</div>")
                .append("</div>")
                .toString();
    }

    private static String escapeHtml(String s) {
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#39;");
    }
}