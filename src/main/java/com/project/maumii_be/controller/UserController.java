package com.project.maumii_be.controller;

import com.project.maumii_be.dto.user.UserRes;
import com.project.maumii_be.dto.user.UserInfoReq;
import com.project.maumii_be.security.CustomMemberDetails;
import com.project.maumii_be.service.user.MailSendingService;
import com.project.maumii_be.service.user.UserCommandService;
import com.project.maumii_be.service.user.UserQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 관련 API")
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    final UserCommandService userCommandService;
    final UserQueryService userQueryService;
    final MailSendingService mailSendingService;

    //사용자 정보 조회
    @GetMapping("/{uId}")
    public ResponseEntity<?> findUserById(@PathVariable String uId){
        ResponseEntity<?> user= userQueryService.findUserById(uId) // Optional<UserRes>
                .map(ResponseEntity::ok)                      // 200 + UserRes
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 (빈 바디)
        log.info("\n사용자 >>>"+user.toString());
        return user;
    }

    //보호자 조회
    @GetMapping("/{uId}/protectors")
    public ResponseEntity<?> getProtectors(@PathVariable("uId") String uId) {
        ResponseEntity<?> protector= ResponseEntity.ok(userQueryService.findProtector(uId));
        log.info("\n 보호자 >>"+protector.toString());
        return protector;
    }

    //전화번호/비밀번호 변경
    @PutMapping("/{uId}/account")
    public ResponseEntity<?> updateAccount(@PathVariable String uId,
                                           @RequestBody UserInfoReq.AccountUpdateReq req) {
        // JWT에서 로그인 유저 uId 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomMemberDetails principal = (CustomMemberDetails) auth.getPrincipal();
        String loginUId = principal.getMember().getUId();

        if (!loginUId.equals(uId)) {
            return ResponseEntity.status(403).body("본인만 수정할 수 있습니다.");
        }

        UserRes result = userCommandService.updateAccount(uId, req);
        // 세션 갱신 제거, 업데이트된 결과만 반환
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{uId}/preference")
    public ResponseEntity<?> updatePreference(@PathVariable String uId,
                                              @RequestBody UserInfoReq.PreferencesUpdateReq req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomMemberDetails principal = (CustomMemberDetails) auth.getPrincipal();
        String loginUId = principal.getMember().getUId();

        if (!loginUId.equals(uId)) {
            return ResponseEntity.status(403).body("본인만 수정할 수 있습니다.");
        }

        UserRes result = userCommandService.updatePreference(uId, req);
        return ResponseEntity.ok(result);
    }

    // 인증코드 발송
    @PostMapping("/{uId}/protectors/send-code")
    public ResponseEntity<?> sendVerificationCode(@PathVariable String uId, @RequestParam String email) {
        try {
            mailSendingService.sendVerificationCode(email);
            return ResponseEntity.ok("인증코드가 발송되었습니다.");
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("이메일 발송에 실패했습니다.");
        }
    }

    // 인증코드 검증
    @PostMapping("/{uId}/protectors/verify-code")
    public ResponseEntity<?> verifyCode(@PathVariable String uId, @RequestParam String email, @RequestParam String code) {
        if (mailSendingService.verifyCode(email, code)) {
            return ResponseEntity.ok("인증이 완료되었습니다.");
        }
        return ResponseEntity.badRequest().body("인증코드가 올바르지 않습니다.");
    }

    //보호자 추가
    @PostMapping("/{uId}/protectors")
    public ResponseEntity<?> addProtector(@PathVariable("uId") String uId
                                        , @RequestParam("pEmail") String pEmail
                                        ,HttpSession session) {

        return ResponseEntity.ok(userCommandService.addProtector(uId,pEmail));
    }

    // 보호자 삭제
    @DeleteMapping("/{uId}/protectors/{pId}")
    public ResponseEntity<?> deleteProtector(@PathVariable("uId") String uId, @PathVariable("pId") Long pId) {
        userCommandService.deleteProtector(uId,pId);
        return ResponseEntity.noContent().build();
    }
}