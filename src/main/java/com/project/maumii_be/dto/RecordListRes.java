package com.project.maumii_be.dto;

import com.project.maumii_be.domain.RecordList;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecordListRes {
    Long rlId;
    String rlName;
    String uId;

    public RecordListRes toRecordListRes(RecordList recordList) {
        return RecordListRes.builder()
                .rlId(recordList.getRlId())
                .rlName(recordList.getRlName())
                .uId(recordList.getUser().getUId())
                .build();
    }
}