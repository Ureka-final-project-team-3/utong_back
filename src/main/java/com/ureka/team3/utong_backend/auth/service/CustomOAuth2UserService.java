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
            logger.info("OAuth2 로그인 시작 - Provider: {}", userRequest.getClientRegistration().getRegistrationId());
            
            OAuth2User oAuth2User = super.loadUser(userRequest);
            logger.info("OAuth2 사용자 정보 조회 성공 - Attributes: {}", oAuth2User.getAttributes());
            
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            logger.error("OAuth2 로그인 처리 중 오류 발생", e);
            throw new OAuth2AuthenticationException("OAuth2 로그인 처리 실패: " + e.getMessage());
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        try {
            String provider = userRequest.getClientRegistration().getRegistrationId();
            logger.info("OAuth2 사용자 처리 시작 - Provider: {}", provider);
            
            // 네이버인 경우 원본 attributes 출력
            if ("naver".equals(provider)) {
                logger.info("네이버 전체 Attributes: {}", oAuth2User.getAttributes());
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
                if (response != null) {
                    logger.info("네이버 response: {}", response);
                    logger.info("네이버 birthday: {}", response.get("birthday"));
                    logger.info("네이버 birth_year: {}", response.get("birth_year"));
                }
            }
            
            OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.of(provider, oAuth2User.getAttributes());
            logger.info("OAuth2 사용자 정보 파싱 완료 - Email: {}, Provider ID: {}, BirthDate: {}, BirthYear: {}", 
                       userInfo.getEmail(), userInfo.getProviderId(), userInfo.getBirthDate(), userInfo.getBirthYear());
            
            Account account = findOrCreateAccount(userInfo);
            logger.info("계정 처리 완료 - Account ID: {}", account.getId());
            
            return new CustomOAuth2UserDto(account, oAuth2User.getAttributes());
        } catch (Exception e) {
            logger.error("OAuth2 사용자 처리 중 오류", e);
            throw new OAuth2AuthenticationException("사용자 처리 실패: " + e.getMessage());
        }
    }
    
    private Account findOrCreateAccount(OAuth2UserInfoDto userInfo) {
        try {
            Account existingAccount = accountRepository.findByProviderAndProviderId(
                    userInfo.getProvider(), userInfo.getProviderId());
            
            if (existingAccount != null) {
                logger.info("기존 OAuth 계정 발견 - Account ID: {}", existingAccount.getId());
                return updateExistingAccount(existingAccount, userInfo);
            }
            
            Account emailAccount = accountRepository.findByEmail(userInfo.getEmail()).orElse(null);
            if (emailAccount != null && emailAccount.getProvider() == null) {
                logger.info("기존 이메일 계정에 OAuth 연동 - Account ID: {}", emailAccount.getId());
                return linkOAuthToExistingAccount(emailAccount, userInfo);
            }
            
            logger.info("새 OAuth 계정 생성 - Email: {}", userInfo.getEmail());
            return createNewOAuthAccount(userInfo);
        } catch (Exception e) {
            logger.error("계정 처리 중 오류", e);
            throw new RuntimeException("계정 처리 실패", e);
        }
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
        logger.info("새 OAuth 계정 저장 완료 - Account ID: {}", savedAccount.getId());
        
        // 네이버인 경우 생년월일 정보 파싱
        LocalDate birthDate = null;
        if ("naver".equals(userInfo.getProvider()) && 
            userInfo.getBirthDate() != null && !"null".equals(userInfo.getBirthDate()) &&
            userInfo.getBirthYear() != null && !"null".equals(userInfo.getBirthYear())) {
            try {
                // 네이버 생년월일 형식: birth_year="1990", birthday="03-15"
                int year = Integer.parseInt(userInfo.getBirthYear());
                String[] parts = userInfo.getBirthDate().split("-");
                if (parts.length == 2) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    birthDate = LocalDate.of(year, month, day);
                    logger.info("네이버 생년월일 파싱 성공: {} (연도: {}, 생일: {})", birthDate, year, userInfo.getBirthDate());
                }
            } catch (Exception e) {
                logger.warn("네이버 생년월일 파싱 실패 - 연도: {}, 생일: {}", userInfo.getBirthYear(), userInfo.getBirthDate(), e);
            }
        }
        
        User user = User.builder()
                .id(userId)
                .name(userInfo.getName())
                .birthDate(birthDate)
                .account(savedAccount)
                .build();
        
        userRepository.save(user);
        logger.info("새 사용자 정보 저장 완료 - User ID: {}, 생년월일: {}", userId, birthDate);
        
        return savedAccount;
    }
}