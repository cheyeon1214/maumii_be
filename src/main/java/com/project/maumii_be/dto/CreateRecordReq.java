package com.project.maumii_be.dto;

import java.util.List;


public record CreateRecordReq(
        Long recordListId,
        Long rLengthMs,
        String rVoiceField,
        List<BubbleReq> bubbles
) {}