package com.ureka.team3.utong_backend.auth.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    
    public CustomOAuth2UserService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            String provider = userRequest.getClientRegistration().getRegistrationId();
            
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            return processOAuth2User(provider, oAuth2User);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("OAuth2 로그인 처리 실패: " + e.getMessage());
        }
    }
    
    private OAuth2User processOAuth2User(String provider, OAuth2User oAuth2User) {
        try {
            logUserAttributes(provider, oAuth2User);
            
            OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.of(provider, oAuth2User.getAttributes());
            
            Account account = findOrCreateAccount(userInfo);
            
            return new CustomOAuth2UserDto(account, oAuth2User.getAttributes());
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("사용자 처리 실패: " + e.getMessage());
        }
    }
    
    private void logUserAttributes(String provider, OAuth2User oAuth2User) {
        switch (provider) {
            case "naver" -> logNaverAttributes(oAuth2User);
            case "kakao" -> logKakaoAttributes(oAuth2User);
            case "google" -> logger.info("구글 사용자 정보: {}", oAuth2User.getAttributes());
        }
    }
    
    private void logNaverAttributes(OAuth2User oAuth2User) {
        Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
        if (response != null) {
            logger.info("네이버 사용자 정보 - Email: {}, Name: {}, Birthday: {}, Birthyear: {}", 
                       response.get("email"), response.get("name"), 
                       response.get("birthday"), response.get("birthyear"));
        }
    }
    
    private void logKakaoAttributes(OAuth2User oAuth2User) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            logger.info("카카오 사용자 정보 - Email: {}, Nickname: {}", 
                       kakaoAccount.get("email"), 
                       profile != null ? profile.get("nickname") : "N/A");
        }
    }
    
    private Account findOrCreateAccount(OAuth2UserInfoDto userInfo) {
        Account existingAccount = accountRepository.findByProviderAndProviderId(
                userInfo.getProvider(), userInfo.getProviderId());
        
        if (existingAccount != null) {
            return updateExistingAccount(existingAccount, userInfo);
        }
        
        Account emailAccount = accountRepository.findByEmail(userInfo.getEmail()).orElse(null);
        if (emailAccount != null && emailAccount.getProvider() == null) {
            logger.info("기존 이메일 계정에 OAuth 연동 - Account ID: {}", emailAccount.getId());
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
                .user(account.getUser())
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
                .user(account.getUser())
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
        
        Account savedAccount = accountRepository.save(account);
        
        LocalDate birthDate = parseBirthDate(userInfo);
        
        User user = User.builder()
                .id(userId)
                .name(userInfo.getName())
                .birthDate(birthDate)
                .account(savedAccount)
                .build();
        
        userRepository.save(user);
        
        return savedAccount;
    }
    
    private LocalDate parseBirthDate(OAuth2UserInfoDto userInfo) {
        if (!"naver".equals(userInfo.getProvider())) {
            return null;
        }
        
        if (userInfo.getBirthDate() == null || "null".equals(userInfo.getBirthDate()) ||
            userInfo.getBirthYear() == null || "null".equals(userInfo.getBirthYear())) {
            return null;
        }
        
        try {
            int year = Integer.parseInt(userInfo.getBirthYear());
            String[] parts = userInfo.getBirthDate().split("-");
            
            if (parts.length == 2) {
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                LocalDate birthDate = LocalDate.of(year, month, day);
                return birthDate;
            }
        } catch (Exception e) {
        	logger.warn("네이버 생년월일 파싱 실패 - 연도: {}, 생일: {}", 
                userInfo.getBirthYear(), userInfo.getBirthDate(), e);
        }
        
        return null;
    }
}