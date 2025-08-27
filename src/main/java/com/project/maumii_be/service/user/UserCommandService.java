package com.project.maumii_be.service.user;

import com.project.maumii_be.domain.Protector;
import com.project.maumii_be.domain.User;
import com.project.maumii_be.domain.enums.Theme;
import com.project.maumii_be.dto.UserRes;
import com.project.maumii_be.dto.user.AccountUpdateReq;
import com.project.maumii_be.dto.user.PreferencesUpdateReq;
import com.project.maumii_be.exception.DMLException;
import com.project.maumii_be.exception.DuplicateException;
import com.project.maumii_be.exception.UserSearchNotException;
import com.project.maumii_be.repository.ProtectorRepository;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {
    //상태를 바꾸는 동작 전달 (C,U,D)
    //보통 @Transactional(readOnly=false)
    final UserRepository userRepository;
    final ProtectorRepository protectorRepository;

    //회원가입
    public void signUp(User user){
        userRepository.save(user);
    }
//    //전화번호 변경
//    public UserRes updateUserPhone(String uId, String uPhone) throws DMLException, UserSearchNotException {
//        User user=userRepository.findById(uId)
//                .orElseThrow(()->new UserSearchNotException("사용자 찾을 수 없음.","NOT_FOUND"));
//        user.setUPhone(uPhone);
//        return new UserRes().toUserRes(user);
//    }
//    //비밀번호 변경
//    public UserRes updateUserPwd(String uId, String uPwd) throws DMLException, UserSearchNotException {
//        User user = userRepository.findById(uId)
//                .orElseThrow(()->new UserSearchNotException("사용자 찾을 수 없음.","NOT_FOUND"));
//        user.setUPwd(uPwd);
//        return new UserRes().toUserRes(user);
//    }

    // 전화번호 & 비밀번호
    public UserRes updateAccount(String uId, AccountUpdateReq req) {
        User user = userRepository.findById(uId)
                .orElseThrow(() -> new UserSearchNotException("사용자를 찾을 수 없습니다.", "NOT_FOUND"));

        if (req.uPhone() != null && !req.uPhone().isBlank()) {
            user.setUPhone(req.uPhone());
        }
        if (req.uPwd() != null && !req.uPwd().isBlank()) {
            user.setUPwd(req.uPwd()); //  인코딩 나중에 해주기
        }
        return new UserRes().toUserRes(user);
    }
    //테마 및 노출 변경
    public UserRes updatePreference(String uId, PreferencesUpdateReq req) throws DMLException, UserSearchNotException {
        User user = userRepository.findById(uId)
                .orElseThrow(()->new UserSearchNotException("사용자 찾을 수 없음.","NOT_FOUND"));
        if (req.uTheme() != null) {
            user.setUTheme(req.uTheme());
        }
        if (req.uExposure() != null) {
            user.setUExposure(req.uExposure());
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
