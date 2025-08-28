package com.project.maumii_be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.User;
import com.project.maumii_be.domain.enums.Theme;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRes {
    @JsonProperty("uId")
    String uId;

    @JsonProperty("uName")
    String uName;

    @JsonProperty("uPhone")
    String uPhone;

    @JsonProperty("uTheme")
    Theme uTheme;

    @JsonProperty("uExposure")
    boolean uExposure;

    public UserRes toUserRes(User user){
        return UserRes.builder()
                .uId(user.getUId())
                .uName(user.getUName())
                .uPhone(user.getUPhone())
                .uTheme(user.getUTheme())
                .uExposure(user.isUExposure())
                .build();
    }

}
