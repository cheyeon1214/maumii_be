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

    @Column(nullable = false, columnDefinition = "varchar(20) default 'cloud'") //DB 기본설정
    @Enumerated(EnumType.STRING)
    @JsonProperty("uTheme")
    private Theme uTheme = Theme.cloud; //java 기본 설정

    @JsonProperty("uExposure")
    private boolean uExposure;

    private String role;

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
