package com.project.maumii_be.dto;

import com.project.maumii_be.domain.Record;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecordRes {
    Long rId;
    LocalDateTime rCreatedAt;
    LocalTime rLength;
    String rVoice;
    Long rlId;

    public RecordRes toRecordRes(Record record) {
        return RecordRes.builder()
                .rId(record.getRId())
                .rCreatedAt(record.getRCreatedAt())
                .rLength(record.getRLength())
                .rVoice(record.getRVoice())
                .rlId(record.getRecordList().getRlId())
                .build();
    }
}
