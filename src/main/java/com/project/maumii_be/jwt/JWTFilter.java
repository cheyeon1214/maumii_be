package com.project.maumii_be.jwt;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.repository.UserRepository;
import com.project.maumii_be.security.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * JWT 검증 필터
 * - PUBLIC 경로(healthz 등)는 아예 필터를 적용하지 않음
 * - Authorization 헤더가 없거나 형식이 이상하면 "그냥 통과" (인증 없이)
 * - 유효한 토큰이면 SecurityContext에 인증 주입
 */
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    // ⬇️ 여기 등록한 경로는 토큰 없어도 항상 통과
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/healthz"
    );

    public JWTFilter(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /** PUBLIC 경로 & OPTIONS 는 필터 자체를 건너뜀 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        // prefix 일치로 체크
        for (String p : PUBLIC_PREFIXES) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        // 헤더가 없으면 인증 없이 통과 (보호된 경로는 결국 401/403을 Security 쪽에서 처리)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer <token>" 형태 보장 체크
        String[] parts = authorization.split(" ", 2);
        if (parts.length != 2 || parts[1] == null || parts[1].isBlank()) {
            // 이상한 헤더면 무시하고 통과 (차라리 미인증으로 처리)
            filterChain.doFilter(request, response);
            return;
        }

        String token = parts[1].trim();

        // 토큰 파싱 중 오류가 나도 "막지 말고" 통과 → 미인증 상태로 컨트롤러/인가 규칙이 판단
        try {
            if (jwtUtil.isExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String id   = jwtUtil.getUId(token);
            // 필요하다면 role/name도 꺼내 쓰기
            // String role = jwtUtil.getRole(token);
            // String name = jwtUtil.getUName(token);

            User member = userRepository.findById(id).orElse(null);
            if (member == null) {
                filterChain.doFilter(request, response);
                return;
            }

            CustomMemberDetails principal = new CustomMemberDetails(member);
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception ignore) {
            // 토큰 형식/서명 오류 등 → 그냥 미인증으로 통과
        }

        filterChain.doFilter(request, response);
    }
}