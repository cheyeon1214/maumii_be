package com.project.maumii_be.dto;

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
    Long bId;
    Boolean bTalker;
    LocalTime bLength;
    Emotion bEmotion;
    String bText;
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
