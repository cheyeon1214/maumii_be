package com.project.maumii_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.enums.Emotion;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BubbleUpdateReq {
    @JsonProperty("bEmotion")
    private Emotion bEmotion;    // "calm", "happy" ... (Enum)

    @JsonProperty("bText")
    private String bText;
}
