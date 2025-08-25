package com.project.maumii_be.domain;

import com.project.maumii_be.domain.enums.Theme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    private String uId;

    @Column(nullable = false)
    private String uPwd;

    @Column(length = 100)
    private String uName;

    @Column(length = 20)
    private String uPhone;

    @Column(nullable = false, columnDefinition = "varchar(20) default 'cloud'") //DB 기본설정
    @Enumerated(EnumType.STRING)
    private Theme uTheme = Theme.cloud; //java 기본 설정

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
