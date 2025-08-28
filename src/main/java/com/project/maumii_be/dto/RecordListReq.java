package com.project.maumii_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.RecordList;
import com.project.maumii_be.domain.User;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecordListReq {
    @JsonProperty("rlName")
    String rlName;

    @JsonProperty("uId")
    String uId;

    public RecordList toRecordList(RecordListReq recordListReq) {
        return RecordList.builder()
                .rlName(recordListReq.getRlName())
                .user(User.builder()
                        .uId(recordListReq.getUId())
                        .build())
                .build();
    }
}
