package com.project.maumii_be.dto;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.domain.enums.Theme;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRes {
    String uId;
    String uName;
    String uPhone;
    Theme uTheme;
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
