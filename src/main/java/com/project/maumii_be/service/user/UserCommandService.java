package com.project.maumii_be.service.user;

import com.project.maumii_be.domain.Protector;
import com.project.maumii_be.domain.User;
import com.project.maumii_be.domain.enums.Theme;
import com.project.maumii_be.dto.user.UserRes;
import com.project.maumii_be.dto.user.UserAuthReq;
import com.project.maumii_be.dto.user.UserInfoReq;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.exception.DuplicateException;
import com.project.maumii_be.exception.UserSearchNotException;
import com.project.maumii_be.repository.ProtectorRepository;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserCommandService {
    //상태를 바꾸는 동작 전달 (C,U,D)
    //보통 @Transactional(readOnly=false)
    final UserRepository userRepository;
    final ProtectorRepository protectorRepository;
    final PasswordEncoder passwordEncoder;

    //회원가입
    public void signUp(UserAuthReq.SignupReq req){
        Theme theme;
        try {
            theme = Theme.valueOf(req.getUName().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            theme = Theme.cloud; // 기본값
        }

        boolean exposure = "all".equalsIgnoreCase(req.getUExposure());

        String pwd=passwordEncoder.encode(req.getUPwd());
        log.info("password->>"+pwd);

        User user = new User();
        user.setUId(req.getUId());
        user.setUPwd(pwd);
        user.setUName(req.getUName());
        user.setUPhone(req.getUPhone());
        user.setUTheme(theme);
        user.setUExposure(exposure);
        userRepository.save(user);
    }

    // 전화번호 & 비밀번호
    public UserRes updateAccount(String uId, UserInfoReq.AccountUpdateReq req) {
        User user = userRepository.findById(uId)
                .orElseThrow(() -> new UserSearchNotException("사용자를 찾을 수 없습니다.", "NOT_FOUND"));

        if (req.getUPhone() != null && !req.getUPhone().isBlank()) {
            user.setUPhone(req.getUPhone());
        }
        if (req.getUPwd() != null && !req.getUPwd().isBlank()) {
            user.setUPwd(passwordEncoder.encode(req.getUPwd())); //  인코딩 나중에 해주기
        }
        return new UserRes().toUserRes(user);
    }

    //테마 및 노출 변경
    public UserRes updatePreference(String uId, UserInfoReq.PreferencesUpdateReq req) throws DMLException, UserSearchNotException {
        User user = userRepository.findById(uId)
                .orElseThrow(()->new UserSearchNotException("사용자 찾을 수 없음.","NOT_FOUND"));
        if (req.getUTheme() != null) {
            user.setUTheme(req.getUTheme());
        }
        if (req.getUExposure() != null) {
            user.setUExposure(req.getUExposure());
        }
        return new UserRes().toUserRes(user);
    }

    //보호자 이메일 등록
    public Long addProtector(String uId,String pEmail) throws DMLException, UserSearchNotException {
        User user=userRepository.findById(uId)
                .orElseThrow(()->new UserSearchNotException("사용자 찾을 수 없음.","NOT_FOUND"));

        boolean isExist = protectorRepository.existsByUserIdAndEmail(uId, pEmail);
        if (isExist) {
            throw new DuplicateException("이미 등록된 이메일입니다.","DUPLICATE");
        }

        Protector protector = Protector.builder()
                            .user(user)
                            .pEmail(pEmail)
                            .build();

        Protector saved =  protectorRepository.save(protector);
        return saved.getPId();
    }

    //보호자 이메일 삭제
    public void deleteProtector(String uId, Long pId) throws DMLException, UserSearchNotException {
        int deleted = protectorRepository.deleteByPIdAndUser_uId(pId,uId);
        if(deleted == 0) {
            throw new DMLException("삭제할 대상이 없습니다.","NOT_FOUND_OR_FORBIDDEN");
        }
    }
}
