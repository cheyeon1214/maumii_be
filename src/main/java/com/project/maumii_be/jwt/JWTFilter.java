package com.project.maumii_be.jwt;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.repository.UserRepository;
import com.project.maumii_be.security.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
  무조건 사용자 요청이 들어올때마다 이 필터를 무조건 제일 먼저 들린다.
  1)토큰 있니?
    있으면 --> 토큰이 유효한지를 체크
    없으면 --> 그냥 다음 해야할 필터 또는 Controller 를 실행
 */
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository; // ★ 주입
    public JWTFilter(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음..인증을 거쳐야하는 서비스에서는 반드시 이 부분이 헤더에 있어야 한다.
    	//헤더에서 Authorization이름의 키값을 꺼내는 작업을 일단 먼저 한다.
        String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) { //인증후 들어온게 아니거나 검증된  토큰이 아니라면

            System.out.println("token null");
            filterChain.doFilter(request, response);//다음에 있는 필터로 가는 부분..갔다가 오면 아래에 있는 사후처리를 하는데..이걸 안하게 하려면 바로 return

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰이 있다면..
        System.out.println("authorization now");
        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            System.out.println("token expired");

            //브라우져로 리플래쉬토큰을 요청

            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //살아있는 토큰이라면 토큰에서 username과 role 획득
        String id = jwtUtil.getUId(token);
        String role = jwtUtil.getRole(token);
        String name = jwtUtil.getUName(token);
        User member = userRepository.findById(id).orElse(null);
        if (member == null) {
            filterChain.doFilter(request, response);
            return;
        }


        //userEntity를 생성하여 값 set
        //스프링 컨테이너에서는 무상태를 유지하고 있기 떄문에 지금 들어온 요청이 누구인지 확인할 수 없다...member객체 생성하는 이유
        //인증된 사용자의 정보를 계속해서 유지하려면 서버가 인증된 사용자의 정보를 알고 있어야 한다.
        //예전에는 세션에서 꺼내썼지만 지금은 토큰에서 뽑아서 Claim에 대한 정보를 꺼내서 member객체를 생성
//        User member = new User();
//        member.setUId(id);
//        member.setUName(name);
//        member.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken =
                new UsernamePasswordAuthenticationToken(customMemberDetails, null, customMemberDetails.getAuthorities());
        //이걸 마지막으로 SecurityContextHolder에 저장  | 세션에 사용자 등록 - 세션이 만들어짐.
        // 이게 저장되어 있으면 이걸 Controller 혹은 Service에서 꺼내어 사용한다.
        // 직접 확인 --> Boardcontroller findAll() 가서 직접 꺼내는 부분을 확인!!
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
/*
jwt토큰을 사용하게 되면 무상태이기 때문에 요청이 들어오는게 누구인지 서버는 알수 없고
아무런 정보가 서버에 저장되어 있지 않기 때문에
서버로 인증된 정보를 가지고 있는 토큰이 들어왔다면
토큰안에 있는 claim정보를 꺼내서 member 객체를 생성하고...Authentication 정보를 만들어서 SecurityContextHolder에 저장해놓아야
Controller나 Service에서 인증된 정보를 받아서 쓰고 빠져나오면 바로 다시 사라지는 Stateless 상태를 유지할수 있다.
다시

Authorization 헤더 검증해서 유효하다면
꺼내서 정보를 member객체로 만들고 authToken생성해서 그걸 SecurityContextHolder에가 저장
그리고나서  filterChain.doFilter()를 호출
또다른 필터가 있으면 필터가 수행..없다면 Controller를 타러 간다.
Controller 안에서 Authorization이 있으면 꺼내서 쓸수 있고
SecurityConfig에 지정한 auth설정에 따라서 Authorization이 반드시 있어야 하는 경우가 발생한다.


*/


























