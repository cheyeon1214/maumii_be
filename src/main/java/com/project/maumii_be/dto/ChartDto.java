package com.project.maumii_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

public class ChartDto {

    @Getter
    @AllArgsConstructor
    public static class EmotionGroupFlowDto {
        private LocalDate date;
        private Long happyCount;
        private Long sadScaredCount;
        private Long angryDisgustCount;
        private Long otherCount;
    }


}