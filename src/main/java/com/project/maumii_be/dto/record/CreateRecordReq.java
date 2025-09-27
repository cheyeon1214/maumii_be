package com.project.maumii_be.dto.record;

import com.project.maumii_be.dto.bubble.BubbleReq;

import java.util.List;


public record CreateRecordReq(
        String voiceField,
        RecordReq record,
        List<BubbleReq> bubbles,
        String recordListTitle
) {}