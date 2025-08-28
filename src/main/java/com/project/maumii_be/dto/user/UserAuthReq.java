package com.project.maumii_be.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

public class UserAuthReq {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignupReq{
        private String uId;
        private String uName;
        private String uPwd;
        private String uPhone;
        private String uTheme;  // "cloud" / "bear"
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
