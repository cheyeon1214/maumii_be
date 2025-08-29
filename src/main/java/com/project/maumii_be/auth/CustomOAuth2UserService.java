// com.project.maumii_be.auth.CustomOAuth2UserService.java
package com.project.maumii_be.auth;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.UserRes;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oAuth2User = super.loadUser(req);
        var attrs = oAuth2User.getAttributes();

        // 카카오 전용 매핑
        KakaoProfile profile = KakaoProfile.from(attrs);
        String provider = "kakao";
        String providerId = profile.id();
        String socialKey = provider + ":" + providerId;

        // 1) 사용자 찾기 (없으면 자동 회원가입)
        User user = userRepository.findBySocialKey(socialKey).orElseGet(() -> {
            User u = new User();
            u.setProvider(provider);
            u.setProviderId(providerId);
            u.setSocialKey(socialKey);
            u.setUName(profile.nickname() != null ? profile.nickname() : "카카오유저");
            u.setEmail(profile.email());
            u.setRole("ROLE_USER");
            return userRepository.save(u);
        });

        // 2) 가벼운 프로필 업데이트 (닉네임/이메일)
        boolean dirty = false;
        if (profile.nickname() != null && !profile.nickname().equals(user.getUName())) {
            user.setUName(profile.nickname());
            dirty = true;
        }
        if (profile.email() != null && !profile.email().equals(user.getEmail())) {
            user.setEmail(profile.email());
            dirty = true;
        }
        if (dirty) userRepository.save(user);

        // 3) SecurityContext에 넣을 OAuth2User 반환
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole())),
                // 프론트로 넘기고 싶은 필드만 노출
                java.util.Map.of(
                        "id", user.getUId(),
                        "name", user.getUName(),
                        "email", user.getEmail(),
                        "provider", user.getProvider()
                ),
                "id" // nameAttributeKey
        );
    }
}