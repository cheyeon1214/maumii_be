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

/*
  무조건 사용자 요청이 들어올 때마다 이 필터를 제일 먼저 탐.
  1) Authorization 헤더 존재 여부 확인
  2) 유효한 JWT면 SecurityContext에 인증 정보 등록
  3) 아니면 그냥 다음 필터로 통과
 */
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public JWTFilter(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String authorization = request.getHeader("Authorization");

        // 헤더가 없거나 Bearer 형식이 아니면 그냥 다음 필터로 넘김
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("[JWTFilter] Token not found or invalid format");
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        // 토큰 만료 확인
        if (jwtUtil.isExpired(token)) {
            System.out.println("[JWTFilter] Token expired");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 정보 추출
        String id = jwtUtil.getUId(token);
        String role = jwtUtil.getRole(token);
        String name = jwtUtil.getUName(token);

        // 사용자 조회
        User member = userRepository.findById(id).orElse(null);
        if (member == null) {
            System.out.println("[JWTFilter] User not found in DB");
            filterChain.doFilter(request, response);
            return;
        }

        // 인증 객체 생성
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customMemberDetails, null, customMemberDetails.getAuthorities());

        // SecurityContextHolder에 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}

/*
요약:
- 모든 요청에 대해 JWT 검증 수행
- /api/healthz 같은 공개 엔드포인트도 무조건 이 필터를 통과
- 토큰이 없으면 그냥 다음 필터로 통과
- 토큰이 있으면 파싱해서 SecurityContext에 등록
- 예외 처리(shouldNotFilter)는 따로 없음
*/