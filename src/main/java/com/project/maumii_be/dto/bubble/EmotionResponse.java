package com.project.maumii_be.dto.bubble;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmotionResponse {
    private String label;
    private double score;
}