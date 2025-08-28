package com.project.maumii_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.Bubble;
import com.project.maumii_be.domain.enums.Emotion;
import lombok.*;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BubbleRes {
    @JsonProperty("bId")
    Long bId;
    @JsonProperty("bTalker")
    String bTalker;
    @JsonProperty("bLength")
    LocalTime bLength;
    @JsonProperty("bEmotion")
    Emotion bEmotion;
    @JsonProperty("bText")
    String bText;
    @JsonProperty("rId")
    Long rId;

    public BubbleRes toBubbleRes(Bubble bubble) {
        return BubbleRes.builder()
                .bId(bubble.getBId())
                .bTalker(bubble.getBTalker())
                .bLength(bubble.getBLength())
                .bEmotion(bubble.getBEmotion())
                .bText(bubble.getBText())
                .rId(bubble.getRecord().getRId())
                .build();
    }
}
