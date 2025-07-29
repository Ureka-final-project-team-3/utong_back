package com.ureka.team3.utong_backend.auth.dto;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ureka.team3.utong_backend.auth.entity.Account;

public class CustomOAuth2UserDto implements OAuth2User {
    
    private final Account account;
    private final Map<String, Object> attributes;
    
    public CustomOAuth2UserDto(Account account, Map<String, Object> attributes) {
        this.account = account;
        this.attributes = attributes;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getName() {
        return account.getId();
    }
    
    public Account getAccount() {
        return account;
    }
}