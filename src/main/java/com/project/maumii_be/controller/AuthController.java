package com.project.maumii_be.controller;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.exception.UserAuthenticationException;
import com.project.maumii_be.repository.UserRepository;
import com.project.maumii_be.service.user.UserCommandService;
import com.project.maumii_be.service.user.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    final UserCommandService userCommandService;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user){
        userCommandService.signUp(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
