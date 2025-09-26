package com.project.maumii_be.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EmojiFlowBuilder {

    public static String buildEmojiFlowHtml(List<Object[]> emotionFLow) {
        // HTML
        return new StringBuilder()
                .append("<html>\n" +
                        "<head>\n" +
                        "<meta charset=\"UTF-8\">\n" +
                        "<title>Insert title here</title>\n" +
                        "<style type=\"text/css\">\n" +
                        "\th1{\n" +
                        "\t\ttext-align: center;\n" +
                        "\t}\n" +
                        "</style>\n" +
                        "<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n" +
                        "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div class=\"container\" style=\"width:650px; height: 300px;\">\n" +
                        "\t<h1>2025년 9월의 감정 흐름 분석 그래프</h1>\n" +
                        "\t<canvas id=\"line-chart\"></canvas>\n" +
                        "</div>\n" +
                        "<script type=\"text/javascript\">\n" +
                        "const ctx = document.querySelector(\"#line-chart\");\n" +
                        "let happy = {\n" +
                        "        label : 'happy',\n" +
                        "        data : [ 5, 10, 2, 4, 3, 7, 1, 2, 4, 1, 5, 10, 2, 4, 3, 7, 1, 2, 4, 1, 5, 10, 2, 4, 3, 7, 1, 2, 4, 1],\n" +
                        "        backgroundColor : 'rgba(255, 191, 0, 0.5)',\n" +
                        "        borderColor : 'rgba(255, 191, 0, 0.5)',\n" +
                        "        borderWidth : 2,\n" +
                        "};\n" +
                        "let sad_scared = {\n" +
                        "        label : 'sad_scared',\n" +
                        "        data : [ 2, 3, 1, 0, 4, 1, 4, 5, 3, 2, 2, 3, 1, 0, 4, 1, 4, 5, 3, 2, 2, 3, 1, 0, 4, 1, 4, 5, 3, 2],\n" +
                        "        backgroundColor : 'rgba(0, 153, 204, 0.5)',\n" +
                        "        borderColor : 'rgba(0, 153, 204, 0.5)',\n" +
                        "        borderWidth : 2,\n" +
                        "};\n" +
                        "let angry_disgust = {\n" +
                        "        label : 'angry_disgust',\n" +
                        "        data : [ 4, 2, 5, 6, 2, 3, 5, 3, 1, 4, 4, 2, 5, 6, 2, 3, 5, 3, 1, 4, 4, 2, 5, 6, 2, 3, 5, 3, 1, 4],\n" +
                        "        backgroundColor : 'rgb(255, 77, 77, 0.5)',\n" +
                        "        borderColor : 'rgb(255, 77, 77, 0.5)',\n" +
                        "        borderWidth : 2,\n" +
                        "};\n" +
                        "let other = {\n" +
                        "        label : 'other',\n" +
                        "        data : [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                        "        backgroundColor : 'black',\n" +
                        "        borderColor : 'black',\n" +
                        "        borderWidth : 2,\n" +
                        "};\n" +
                        "new Chart(ctx, {\n" +
                        "\ttype:'line',\n" +
                        "\tdata: {\n" +
                        "\t\tlabels:['1', '2', '3', '4', '5', '6', '7', '8', '9', '10','11', '12', '13', '14', '5', '6', '7', '8', '9', '10','1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],\n" +
                        "\t\tdatasets:[happy, sad_scared, angry_disgust],\n" +
                        "\t},\n" +
                        "\toptions:{\n" +
                        "\t\tscales:{\n" +
                        "\t\t\ty:{\n" +
                        "\t\t\t\ttype: 'linear',\n" +
                        "\t\t\t\tmin: 0,\n" +
                        "\t\t\t\tmax: 20,\n" +
                        "\t\t\t}\n" +
                        "\t\t},\n" +
                        "\t}\n" +
                        "});\n" +
                        "</script>\n" +
                        "</body>\n" +
                        "</html>")
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