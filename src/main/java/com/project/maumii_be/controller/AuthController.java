package com.project.maumii_be.controller;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.UserRes;
import com.project.maumii_be.dto.user.UserAuthReq;
import com.project.maumii_be.exception.UserSearchNotException;
import com.project.maumii_be.repository.UserRepository;
import com.project.maumii_be.security.CustomMemberDetails;
import com.project.maumii_be.service.user.UserCommandService;
import com.project.maumii_be.service.user.UserQueryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    final UserCommandService userCommandService;
    final UserQueryService userQueryService;
    final PasswordEncoder passwordEncoder;
    final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserAuthReq.SignupReq req){
        log.info(req.toString());
        userCommandService.signUp(req);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/signin")
//    public ResponseEntity<?> signin(@RequestBody UserAuthReq.SigninReq req, HttpSession session) {
//        log.info("\n 로그인 사용자 >>>> "+req.toString());
//        User user= userRepository.findById(req.getUId())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디/비밀번호를 확인하세요."));
//
//
//        if (!passwordEncoder.matches(req.getUPwd(), user.getUPwd())) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디/비밀번호를 확인하세요.");
//        }
//
//        UserRes sessionUser = new UserRes(
//                user.getUId(),
//                user.getUName(),
//                user.getUPhone(),
//                user.getUTheme(),
//                user.isUExposure()
//        );
//
//        session.setAttribute("LOGIN_USER", sessionUser);   // ★ 세션 저장
//        return ResponseEntity.ok(sessionUser);
//    }

//    @GetMapping("/me")
//    public ResponseEntity<?> me(HttpSession session) {
//        log.info("=== /me 호출 - 세션 확인 ===");
//        UserRes su = (UserRes) session.getAttribute("LOGIN_USER");
//        log.info("세션에서 가져온 사용자 정보: {}", su);
//
//        if (su == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        return ResponseEntity.ok(su); // 사용자 객체 반환
//    }
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomMemberDetails principal = (CustomMemberDetails) auth.getPrincipal();
        User u = principal.getMember();

        UserRes res = new UserRes(
                u.getUId(),
                u.getUName(),
                u.getUPhone(),
                u.getUTheme(),
                u.isUExposure()
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // 서버 상태가 없어 특별히 할 일 없음. (블랙리스트 운영 시 여기에 추가)
        return ResponseEntity.ok().build();
    }
}
