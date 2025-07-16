package com.ureka.team3.utong_backend.gift.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.user.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.user.repository.UserRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.common.exception.business.AccountNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.ConcurrentAccessException;
import com.ureka.team3.utong_backend.common.exception.business.GifticonNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.gift.dto.GifticonResponseDto;
import com.ureka.team3.utong_backend.gift.entity.Gifticon;
import com.ureka.team3.utong_backend.gift.entity.UserGifticon;
import com.ureka.team3.utong_backend.gift.repository.GifticonRepository;
import com.ureka.team3.utong_backend.gift.repository.MyGifticonRepository;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GifticonServiceImpl implements GifticonService {

    private final GifticonRepository gifticonRepository;

    private final MyGifticonRepository myGifticonRepository;

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    @Override
    public ApiResponse<List<GifticonResponseDto>> getGifticonList() {
        try {
            List<GifticonResponseDto> list = gifticonRepository.findAll()
                    .stream()
                    .map(GifticonResponseDto::from)
                    .toList();

            return ApiResponse.success(list);
        } catch (Exception e) {
            log.error("기프티콘 목록 조회 중 오류가 발생하였습니다. {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<GifticonResponseDto> getGifticonDetail(String gifticonId) {
        try {
            GifticonResponseDto gifticon = gifticonRepository.findById(gifticonId)
                    .map(GifticonResponseDto::from)
                    .orElseThrow(() -> new BusinessException(ErrorCode.GIFTICON_NOT_FOUND));

            return ApiResponse.success(gifticon);
        } catch (Exception e) {
            log.error("기프티콘 상세 조회 중 오류가 발생하였습니다. {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<Long> getGifticonCount() {
        try {
            Long count = gifticonRepository.count();

            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("기프티콘 개수 조회 중 오류가 발생하였습니다. {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> exchangeGifticon(String gifticonId, String accountId) {
        try {
            Account account = accountRepository.findByIdWithLock(accountId)
                    .orElseThrow(AccountNotFoundException::new);

            Gifticon gifticon = gifticonRepository.findById(gifticonId)
                    .orElseThrow(GifticonNotFoundException::new);

            User user = userRepository.findByAccountId(accountId)
                    .orElseThrow(UserNotFoundException::new);

            account.decreasePoint(gifticon.getPrice());

            UserGifticon userGifticon = UserGifticon.builder()
                    .user(user)
                    .gifticon(gifticon)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.now().plusDays(30).toLocalDate().atTime(23, 59, 59))
                    .build();

            myGifticonRepository.save(userGifticon);

            log.info("기프티콘 교환 성공: gifticonId = {}, accountId = {}, price = {}", gifticonId, accountId, gifticon.getPrice());

            return ApiResponse.success(null);
        } catch (PessimisticLockException e) {
            log.warn("동시 접근으로 인한 락 타임아웃: accountId = {}", accountId, e);
            throw new ConcurrentAccessException();
        } catch (AccountNotFoundException | GifticonNotFoundException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("기프티콘 교환 중 오류가 발생하였습니다. gifticonId = {}, accountId = {}, error = {}", gifticonId, accountId, e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
