package com.project.maumii_be.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

public class UserAuthReq {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignupReq{
        @JsonProperty("uId")
        private String uId;
        @JsonProperty("uName")
        private String uName;
        @JsonProperty("uPwd")
        private String uPwd;
        @JsonProperty("uPhone")
        private String uPhone;
        @JsonProperty("uTheme")
        private String uTheme;  // "cloud" / "bear"
        @JsonProperty("uExposure")
        private String uExposure;    // "all" / "calm"
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class SigninReq{
        @JsonProperty("uId")
        private String uId;
        @JsonProperty("uPwd")
        private String uPwd;
    }
}
