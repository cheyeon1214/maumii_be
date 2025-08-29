package com.project.maumii_be.config;

import io.jsonwebtoken.security.Password;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/signup",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(user -> user.userService(kakaoOAuth2UserService()))
                        .successHandler((req, res, auth) -> {
                            // 남은 oauth 속성 정리 (중복 재시도 차단)
                            req.getSession(false); // 존재하면
                            if (req.getSession(false) != null) {
                                req.getSession(false).removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
                            }
                            // 로그인 성공 후 프론트 라우트로 바로!
                            res.sendRedirect("http://localhost:5050/record");
                        })
                        .failureHandler((req, res, ex) -> {
                            ex.printStackTrace();
                            // 에러 메시지를 쿼리 파라미터로 추가
                            res.sendRedirect("http://localhost:5050/login?error=OAuthLoginFailed");
                        })
                );

        return http.build();
    }

    // 카카오 사용자 정보 매핑
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> kakaoOAuth2UserService() {
        return userRequest -> {
            var delegate = new DefaultOAuth2UserService();
            OAuth2User user = delegate.loadUser(userRequest);

            Map<String, Object> attrs = user.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attrs.get("kakao_account");
            Map<String, Object> profile = kakaoAccount != null
                    ? (Map<String, Object>) kakaoAccount.get("profile")
                    : null;

            String id = String.valueOf(attrs.get("id"));
            String nickname = profile != null ? (String) profile.getOrDefault("nickname", "") : "";
            String email = kakaoAccount != null ? (String) kakaoAccount.getOrDefault("email", "") : "";

            // 필요한 정보만 앱에서 쓰기 쉽게 재구성
            Map<String, Object> mapped = Map.of(
                    "id", id,
                    "nickname", nickname,
                    "email", email
            );

            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    mapped,
                    "id"
            );
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5050", "http://localhost:9000"));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

