package com.project.maumii_be.service.user;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.dto.UserRes;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserQueryService {
    //목록/상세/검색, 캐시/읽기 DB
    //보통 @Transactional(readOnly = true)
    final UserRepository userRepository;

    public UserRes findById(String uId) throws Exception{
        User u= userRepository.findById(uId).orElseThrow();
        UserRes user = new UserRes().toUserRes(u);
        return user;
    }




}
