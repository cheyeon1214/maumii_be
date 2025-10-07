package com.project.maumii_be.jwt;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.repository.UserRepository;
import com.project.maumii_be.security.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public JWTFilter(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // 필터를 거치지 않을 공개 경로
    private static final List<String> PUBLIC = List.of(
            "/api/healthz",
            "/api/auth/**",
            "/api/users/**",
            "/api/sms/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/ws/**"
    );
    private static final AntPathMatcher PM = new AntPathMatcher();

    private boolean isPublic(String path) {
        for (String p : PUBLIC) if (PM.match(p, path)) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // 1) 프리플라이트는 무조건 통과
        if (HttpMethod.OPTIONS.matches(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // 2) 공개 경로는 통과
        String uri = req.getRequestURI();
        if (isPublic(uri)) {
            chain.doFilter(req, res);
            return;
        }

        // 3) 인증 필요 경로는 토큰 확인
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            // 여기서 바로 401 내려주면 Security에서 403으로 바뀌는 혼선을 막을 수 있음
            unauthorized(res, "NO_TOKEN");
            return;
        }

        String token = auth.substring(7);

        // 4) 만료/유효성 검사
        if (jwtUtil.isExpired(token)) {
            unauthorized(res, "TOKEN_EXPIRED");
            return;
        }

        String uId;
        try {
            uId = jwtUtil.getUId(token);
        } catch (Exception e) {
            unauthorized(res, "TOKEN_INVALID");
            return;
        }

        User user = userRepository.findById(uId).orElse(null);
        if (user == null) {
            unauthorized(res, "USER_NOT_FOUND");
            return;
        }

        // 5) SecurityContext에 인증 객체 주입
        var principal = new CustomMemberDetails(user);
        Authentication authToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        chain.doFilter(req, res);
    }

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getOutputStream().write(("{\"error\":\"" + msg + "\"}")
                .getBytes(StandardCharsets.UTF_8));
    }
}