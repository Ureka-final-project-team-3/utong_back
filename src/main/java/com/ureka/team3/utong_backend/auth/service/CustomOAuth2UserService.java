package com.ureka.team3.utong_backend.auth.service;


import java.util.UUID;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ureka.team3.utong_backend.auth.dto.CustomOAuth2UserDto;
import com.ureka.team3.utong_backend.auth.dto.OAuth2UserInfoDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    
    public CustomOAuth2UserService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        return processOAuth2User(userRequest, oAuth2User);
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.of(provider, oAuth2User.getAttributes());
        
        Account account = findOrCreateAccount(userInfo);
        
        return new CustomOAuth2UserDto(account, oAuth2User.getAttributes());
    }
    
    private Account findOrCreateAccount(OAuth2UserInfoDto userInfo) {
        Account existingAccount = accountRepository.findByProviderAndProviderId(
                userInfo.getProvider(), userInfo.getProviderId());
        
        if (existingAccount != null) {
            return updateExistingAccount(existingAccount, userInfo);
        }
        
        Account emailAccount = accountRepository.findByEmail(userInfo.getEmail()).orElse(null);
        if (emailAccount != null && emailAccount.getProvider() == null) {
            return linkOAuthToExistingAccount(emailAccount, userInfo);
        }
        
        return createNewOAuthAccount(userInfo);
    }
    
    private Account updateExistingAccount(Account account, OAuth2UserInfoDto userInfo) {
        Account updatedAccount = Account.builder()
                .id(account.getId())
                .email(userInfo.getEmail())
                .nickname(userInfo.getName())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .mileage(account.getMileage())
                .password(account.getPassword())
                .build();
        
        return accountRepository.save(updatedAccount);
    }
    
    private Account linkOAuthToExistingAccount(Account account, OAuth2UserInfoDto userInfo) {
        Account linkedAccount = Account.builder()
                .id(account.getId())
                .email(account.getEmail())
                .nickname(account.getNickname())
                .password(account.getPassword())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .mileage(account.getMileage())
                .build();
        
        return accountRepository.save(linkedAccount);
    }
    
    private Account createNewOAuthAccount(OAuth2UserInfoDto userInfo) {
        String accountId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        
        Account account = Account.builder()
                .id(accountId)
                .email(userInfo.getEmail())
                .nickname(userInfo.getName())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .mileage(0L)
                .build();
        
        accountRepository.save(account);
        
        User user = User.builder()
                .id(userId)
                .name(userInfo.getName())
                .account(account)
                .build();
        
        userRepository.save(user);
        
        return account;
    }
}