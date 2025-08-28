package com.project.maumii_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.RecordList;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecordListRes {
    @JsonProperty("rlId")
    Long rlId;

    @JsonProperty("rlName")
    String rlName;

    @JsonProperty("updateDate")
    LocalDateTime updateDate;

    @JsonProperty("uId")
    String uId;

    public RecordListRes toRecordListRes(RecordList recordList) {
        return RecordListRes.builder()
                .rlId(recordList.getRlId())
                .rlName(recordList.getRlName())
                .updateDate(recordList.getUpdateDate())
                .uId(recordList.getUser().getUId())
                .build();
    }
}