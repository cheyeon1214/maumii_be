package com.project.maumii_be.dto.record;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("rLength")
    LocalTime rLength;

    @JsonProperty("rVoice")
    String rVoice;

    @JsonProperty("rlId")
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
