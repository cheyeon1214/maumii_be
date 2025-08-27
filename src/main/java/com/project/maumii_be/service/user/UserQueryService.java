package com.project.maumii_be.service.user;

import com.project.maumii_be.domain.Protector;
import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.ProtectorRes;
import com.project.maumii_be.dto.UserRes;
import com.project.maumii_be.exception.UserAuthenticationException;
import com.project.maumii_be.exception.UserSearchNotException;
import com.project.maumii_be.repository.ProtectorRepository;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //user 정보 조회 및 아이디 중복체크
    public UserRes findUserById(String uId) throws UserSearchNotException {
        User u= userRepository.findById(uId).orElseThrow(()->new UserSearchNotException("사용자를 찾을 수 없습니다.","NOT_FOUND"));
        UserRes user = new UserRes().toUserRes(u);
        return user;
    }

    //로그인
    public UserRes signIn(String uId, String uPwd) throws UserAuthenticationException{
        User ruser = userRepository.login(uId,uPwd).orElseThrow(()->new UserAuthenticationException("사용자 정보가 없습니다. 다시 확인해주세요.","Wrong ID"));
        if(ruser.getUPwd().equals(uPwd)){
            throw new UserAuthenticationException("비밀번호가 올바르지 않습니다. ","Wrong Password");
        }
        UserRes user = new UserRes().toUserRes(ruser);
        return user;
    }

    //보호자 정보 불러오기
    public List<ProtectorRes> findProtector(String uId) throws UserSearchNotException {
        List<Protector> list = protectorRepository.findByUser_uId(uId);
        if(list == null || list.isEmpty()) {
            throw new UserSearchNotException("사용자 정보가 없습니다. 다시 확인해주세요.","EMPTY USER");
        }
        return list.stream().map(ProtectorRes::new).collect(Collectors.toList());
    }



}
