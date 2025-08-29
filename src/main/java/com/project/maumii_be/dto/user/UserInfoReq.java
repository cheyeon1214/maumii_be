package com.project.maumii_be.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("uPhone")
        private String uPhone;
        @JsonProperty("uPwd")
        private String uPwd;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferencesUpdateReq{
        @JsonProperty("uTheme")
        private Theme uTheme;
        @JsonProperty("uExposure")
        private Boolean uExposure;
    }
}
