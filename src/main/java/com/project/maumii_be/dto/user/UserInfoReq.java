package com.project.maumii_be.dto.user;

import com.project.maumii_be.domain.enums.Theme;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserInfoReq {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountUpdateReq{
        private String uPhone;
        private String uPwd;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferencesUpdateReq{
        private Theme uTheme;
        private Boolean uExposure;
    }
}
