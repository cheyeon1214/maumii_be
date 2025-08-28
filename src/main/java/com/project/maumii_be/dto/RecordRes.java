package com.project.maumii_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.enums.Emotion;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordRes {
    @JsonProperty("rId")
    private Long rId;

    @JsonProperty("rCreatedAt")
    private LocalDateTime rCreatedAt;

    @JsonProperty("rLength")
    private LocalTime rLength;

    @JsonProperty("rVoice")
    private String rVoice;

    @JsonProperty("rlId")
    private Long rlId;

    @JsonProperty("rlName")
    private String rlName;

    @JsonProperty("bubbles")
    private List<BubbleRes> bubbles;   // ✅ 여기 타입 확실히!

    // RecordRes.java
    public static RecordRes toRecordRes(Record record) {
        return RecordRes.builder()
                .rId(record.getRId())
                .rCreatedAt(record.getRCreatedAt())
                .rLength(record.getRLength())
                .rVoice(record.getRVoice())
                .rlName(record.getRecordList() != null ? record.getRecordList().getRlName() : null)
                .rlId(record.getRecordList() != null ? record.getRecordList().getRlId() : null)
                .bubbles(record.getBubbles() == null ? List.of() :
                        record.getBubbles().stream().map(b -> BubbleRes.builder()
                                .bId(b.getBId())
                                .bTalker(b.getBTalker())
                                .bText(b.getBText())
                                .bEmotion(b.getBEmotion())
                                .bLength(b.getBLength())
                                .rId(record.getRId())
                                .build()
                        ).collect(Collectors.toList()))
                .build();
    }
}