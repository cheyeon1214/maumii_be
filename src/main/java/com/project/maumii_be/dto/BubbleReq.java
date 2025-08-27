// src/main/java/com/project/maumii_be/dto/BubbleReq.java
package com.project.maumii_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.enums.Emotion;
import lombok.*;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BubbleReq {

    @JsonProperty("bTalker")
    private String bTalker;

    // true=me, false=partner
    @JsonProperty("bLength")
    private java.time.LocalTime bLength;   // (선택) 프론트가 LocalTime로 직접 줄 때

    @JsonProperty("bEmotion")
    private Emotion bEmotion;    // "calm", "happy" ... (Enum)

    @JsonProperty("bText")
    private String bText;

    @JsonProperty("rId")
    private Long rId;            // 사용 안 해도 됨(서비스에서 강제 세팅)

    private String fileField;    // ✅ 오디오 파일 파트 이름 (예: "audio_0")
    private Long durationMs;     // ✅ 프론트가 ms로 주면 서버에서 LocalTime으로 변환

    public Bubble toBubble(BubbleReq bubbleReq) {
        return Bubble.builder()
                .bTalker(bubbleReq.getBTalker())
                .bLength(bubbleReq.getBLength())   // null이면 서비스에서 durationMs로 변환해 세팅
                .bEmotion(bubbleReq.getBEmotion())
                .bText(bubbleReq.getBText())
                .record(Record.builder().rId(bubbleReq.getRId()).build())
                .build();
    }
}