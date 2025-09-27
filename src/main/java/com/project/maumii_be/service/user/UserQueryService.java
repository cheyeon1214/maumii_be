package com.project.maumii_be.service.user;

import com.project.maumii_be.domain.Protector;
import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.user.ProtectorRes;
import com.project.maumii_be.dto.user.UserRes;
import com.project.maumii_be.dto.user.UserAuthReq;
import com.project.maumii_be.exception.UserAuthenticationException;
import com.project.maumii_be.exception.UserSearchNotException;
import com.project.maumii_be.repository.ProtectorRepository;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    //목록/상세/검색, 캐시/읽기 DB
    //보통 @Transactional(readOnly = true)
    final UserRepository userRepository;
    final ProtectorRepository protectorRepository;
    final PasswordEncoder passwordEncoder;

    //user 정보 조회 및 아이디 중복체크
    public Optional<UserRes> findUserById(String uId) {
        return userRepository.findById(uId)
                .map(user -> new UserRes().toUserRes(user));
    }

    //로그인
    public UserRes signIn(UserAuthReq.SigninReq req) throws UserAuthenticationException{
        User ruser = userRepository.login(req.getUId(),req.getUPwd()).orElseThrow(()->new UserAuthenticationException("사용자 정보가 없습니다. 다시 확인해주세요.","Wrong ID"));
        if(!passwordEncoder.matches(req.getUPwd(),ruser.getUPwd())) {
            throw new UserAuthenticationException("비밀번호가 올바르지 않습니다. ","Wrong Password");
        }
        UserRes user = new UserRes().toUserRes(ruser);
        return user;
    }

    //보호자 정보 불러오기
    public List<ProtectorRes> findProtector(String uId) throws UserSearchNotException {
        // 먼저 사용자가 존재하는지 확인
        User user = userRepository.findById(uId)
                .orElseThrow(() -> new UserSearchNotException("사용자 정보가 없습니다. 다시 확인해주세요.", "USER_NOT_FOUND"));

        List<Protector> list = protectorRepository.findByUser_uId(uId);

        // 보호자가 없어도 빈 리스트 반환 (예외 발생 안함)
        if(list == null) {
            return new ArrayList<>();
        }

        return list.stream().map(ProtectorRes::new).collect(Collectors.toList());
    }
}