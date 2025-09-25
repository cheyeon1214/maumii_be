package com.project.maumii_be.security;

import com.project.maumii_be.domain.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Slf4j
public class CustomMemberDetails implements UserDetails {

    @Getter
    private final User member;

    public CustomMemberDetails(User member) {
        this.member = member;
        log.info("CustomMemberDetails===>{}", member);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = member.getRole();
        if (role == null || role.isBlank()) role = "USER";
        if (!role.startsWith("ROLE_")) role = "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getPassword() { return member.getUPwd(); }
    @Override public String getUsername() { return member.getUId(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}