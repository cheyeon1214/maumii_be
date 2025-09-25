package com.project.maumii_be.security;

import com.project.maumii_be.domain.User;
import com.project.maumii_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//Repository의 함수를 호출...
@RequiredArgsConstructor
@Service
@Slf4j
public class CustomMemberDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username = uId 로 들어옵니다 (LoginFilter에서 uId를 principal로 넣었기 때문)
        return userRepository.findById(username)
                .map(CustomMemberDetails::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));
    }
}
