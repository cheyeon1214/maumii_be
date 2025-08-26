package com.project.maumii_be.dto;

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
    Boolean bTalker;
    LocalTime bLength;
    Emotion bEmotion;
    String bText;
    Long rId;

    public Bubble toBubble(BubbleReq bubbleReq) {
        return Bubble.builder()
                .bTalker(bubbleReq.getBTalker())
                .bLength(bubbleReq.getBLength())
                .bEmotion(bubbleReq.getBEmotion())
                .bText(bubbleReq.getBText())
                .record(Record.builder()
                        .rId(bubbleReq.getRId())
                        .build())
                .build();
    }

}