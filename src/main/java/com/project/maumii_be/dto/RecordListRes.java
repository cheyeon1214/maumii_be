package com.project.maumii_be.dto;

import com.project.maumii_be.domain.RecordList;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecordListRes {
    Long rlId;
    String rlName;
    LocalDateTime updateDate;
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