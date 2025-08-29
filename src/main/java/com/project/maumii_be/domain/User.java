package com.project.maumii_be.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.maumii_be.domain.enums.Theme;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "users")
public class User {
    @Id
    @JsonProperty("uId")
    private String uId;

    @Column(nullable = false)
    @JsonProperty("uPwd")
    private String uPwd;

    @Column(length = 100)
    @JsonProperty("uName")
    private String uName;

    @Column(length = 20)
    @JsonProperty("uPhone")
    private String uPhone;

    // 소셜용
    private String provider;      // "kakao"
    private String providerId;    // 카카오에서 내려주는 id (문자열로 저장 추천)
    // 편의: 소셜로만 가입한 사용자도 유니크하게 보장
    @Column(unique = true)
    private String socialKey;     // provider + ":" + providerId

    private String email;

    private String role = "ROLE_USER";

    @Column(nullable = false, columnDefinition = "varchar(20) default 'cloud'") //DB 기본설정
    @Enumerated(EnumType.STRING)
    @JsonProperty("uTheme")
    private Theme uTheme = Theme.cloud; //java 기본 설정

    @JsonProperty("uExposure")
    private boolean uExposure;

    @Override
    public String toString() {
        return "User{" +
                "uId='" + uId + '\'' +
                ", uPwd='" + uPwd + '\'' +
                ", uName='" + uName + '\'' +
                ", uPhone='" + uPhone + '\'' +
                ", uTheme=" + uTheme +
                ", uExposure=" + uExposure +
                '}';
    }
}
