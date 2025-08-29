// com.project.maumii_be.auth.KakaoProfile.java
package com.project.maumii_be.auth;

import java.util.Map;

public record KakaoProfile(
        String id, String nickname, String email
) {
    @SuppressWarnings("unchecked")
    public static KakaoProfile from(Map<String, Object> attributes) {
        String id = String.valueOf(attributes.get("id"));

        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        String email = account != null ? (String) account.get("email") : null;

        Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;
        String nickname = profile != null ? (String) profile.get("nickname") : null;

        return new KakaoProfile(id, nickname, email);
    }
}