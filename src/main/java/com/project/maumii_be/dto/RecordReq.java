package com.project.maumii_be.dto;

import com.project.maumii_be.domain.Record;
import com.project.maumii_be.domain.RecordList;
import lombok.*;

import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecordReq {
    LocalTime rLength;
    String rVoice;
    Long rlId;

    public Record toRecord(RecordReq recordReq) {
        return Record.builder()
                .rLength(recordReq.getRLength())
                .rVoice(recordReq.getRVoice())
                .recordList(RecordList.builder()
                        .rlId(recordReq.getRlId())
                        .build())
                .build();
    }
}
