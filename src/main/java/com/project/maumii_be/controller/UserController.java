package com.project.maumii_be.controller;

import com.project.maumii_be.dto.user.UserInfoReq;
import com.project.maumii_be.service.user.UserCommandService;
import com.project.maumii_be.service.user.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    final UserCommandService userCommandService;
    final UserQueryService userQueryService;

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
    public ResponseEntity<?> updateAccount(@PathVariable("uId") String uId,@RequestBody UserInfoReq.AccountUpdateReq req) {
        return ResponseEntity.ok(userCommandService.updateAccount(uId, req));
    }

    //테마,노출범위 변경
    @PutMapping("/{uId}/preference")
    public ResponseEntity<?> updatePreference(@PathVariable("uId") String uId,@RequestBody UserInfoReq.PreferencesUpdateReq req){
        return ResponseEntity.ok(userCommandService.updatePreference(uId, req));
    }

    //보호자 추가
    @PostMapping("/{uId}/protectors")
    public ResponseEntity<?> addProtector(@PathVariable("uId") String uId, @RequestParam("pEmail") String pEmail) {
        return ResponseEntity.ok(userCommandService.addProtector(uId,pEmail));
    }
    // 보호자 삭제

    @DeleteMapping("/{uId}/protectors/{pId}")
    public ResponseEntity<?> deleteProtector(@PathVariable("uId") String uId, @PathVariable("pId") Long pId) {
        userCommandService.deleteProtector(uId,pId);
        return ResponseEntity.noContent().build();
    }


}
