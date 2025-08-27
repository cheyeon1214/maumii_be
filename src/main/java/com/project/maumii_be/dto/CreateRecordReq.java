package com.project.maumii_be.dto;

import java.util.List;


public record CreateRecordReq(
        String voiceField,
        RecordReq record,
        List<BubbleReq> bubbles,
        String recordListTitle
) {}