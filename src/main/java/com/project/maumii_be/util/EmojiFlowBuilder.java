package com.project.maumii_be.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmojiFlowBuilder {

    public static String buildEmojiFlowHtml(List<Object[]> emotionFlow) {
        List<String> labels = new ArrayList<>();
        List<Integer> happyData = new ArrayList<>();
        List<Integer> sadScaredData = new ArrayList<>();
        List<Integer> angryDisgustData = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d"); // 날짜만 (1~31)

        for (Object[] row : emotionFlow) {
            // 0번: java.sql.Date → String label
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            String dayLabel = String.valueOf(sqlDate.toLocalDate().getDayOfMonth());
            labels.add("\"" + dayLabel + "\""); // JSON 문자열용 따옴표 포함

            // 1~3번: counts
            happyData.add(((Number) row[1]).intValue());
            sadScaredData.add(((Number) row[2]).intValue());
            angryDisgustData.add(((Number) row[3]).intValue());
        }

        // JSON 배열 문자열화
        String labelsJson = String.join(",", labels);
        String happyJson = happyData.toString();
        String sadJson = sadScaredData.toString();
        String angryJson = angryDisgustData.toString();

        String json = """
        {
          "type": "line",
          "data": {
            "labels": [%s],
            "datasets": [
              {
                "label": "happy",
                "data": %s,
                "borderColor": "rgba(255,191,0,0.8)",
                "backgroundColor": "rgba(255,191,0,0.15)",
                "fill": false,
                "tension": 0.25
              },
              {
                "label": "sad_scared",
                "data": %s,
                "borderColor": "rgba(0,153,204,0.8)",
                "backgroundColor": "rgba(0,153,204,0.15)",
                "fill": false,
                "tension": 0.25
              },
              {
                "label": "angry_disgust",
                "data": %s,
                "borderColor": "rgba(255,77,77,0.8)",
                "backgroundColor": "rgba(255,77,77,0.15)",
                "fill": false,
                "tension": 0.25
              }
            ]
          },
          "options": {
            "plugins": { "legend": { "position": "bottom" } },
            "scales": { "y": { "min": 0 } }
          }
        }
        """.formatted(labelsJson, happyJson, sadJson, angryJson);

        // URL 인코딩
        String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);

        String chartUrl = "https://quickchart.io/chart"
                + "?c=" + encoded
                + "&format=png&width=650&height=300&devicePixelRatio=2&version=4.4.0";

        return """
<div style='width:100%; text-align:center;'>
  <h1 style="font-size:18px; margin:8px 0 12px; text-align:center;">
    감정 흐름 분석 그래프
  </h1>
  <img src='""" + chartUrl + "' style='display:block; margin:0 auto; max-width:650px; height:auto;' alt='Emotion Flow Chart'/>"
                + "</div>";
    }
}