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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GifticonServiceTest {

    @Mock
    private GifticonRepository gifticonRepository;

    @Mock
    private MyGifticonRepository myGifticonRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GifticonServiceImpl gifticonService;

    private Gifticon createTestGifticon(Long price) {
        return Gifticon.builder()
                .id(UUID.randomUUID().toString())
                .name("테스트 기프티콘")
                .price(price)
                .description("테스트 설명")
                .imageUrl("http://test.com/image.jpg")
                .imageKey("test-image-key")
                .build();
    }

    private Account createTestAccount(String id, Long mileage) {
        return Account.builder()
                .id(id)
                .nickname("테스트유저")
                .email("test@example.com")
                .mileage(mileage)
                .build();
    }

    private User createTestUser(String id, Account account) {
        return User.builder()
                .id(id)
                .name("테스트유저")
                .birthDate(LocalDate.of(1990, 1, 1))
                .account(account)
                .build();
    }

    @Nested
    @DisplayName("기프티콘 목록 조회")
    class GetGifticonList {
        @Test
        @DisplayName("성공")
        void getGifticonList_성공_test() {
            // given
            Gifticon gifticon = createTestGifticon(10000L);
            List<Gifticon> gifticons = List.of(gifticon);
            given(gifticonRepository.findAll()).willReturn(gifticons);

            // when
            ApiResponse<List<GifticonResponseDto>> response = gifticonService.getGifticonList();

            // then
            assertThat(response.getResultCode()).isEqualTo(200);
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getData().get(0).getName()).isEqualTo(gifticon.getName());
        }

        @Test
        @DisplayName("실패 - 서버 오류")
        void getGifticonList_실패_서버오류_test() {
            // given
            given(gifticonRepository.findAll()).willThrow(new RuntimeException("DB 조회 오류"));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> gifticonService.getGifticonList());
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("기프티콘 상세 조회")
    class GetGifticonDetail {
        @Test
        @DisplayName("성공")
        void getGifticonDetail_성공_test() {
            // given
            Gifticon gifticon = createTestGifticon(10000L);
            String gifticonId = gifticon.getId();
            given(gifticonRepository.findById(gifticonId)).willReturn(Optional.of(gifticon));

            // when
            ApiResponse<GifticonResponseDto> response = gifticonService.getGifticonDetail(gifticonId);

            // then
            assertThat(response.getResultCode()).isEqualTo(200);
            assertThat(response.getData().getId()).isEqualTo(gifticonId);
            assertThat(response.getData().getName()).isEqualTo(gifticon.getName());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 기프티콘")
        void getGifticonDetail_실패_기프티콘없음_test() {
            // given
            String nonExistentId = "non-existent-id";
            given(gifticonRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> gifticonService.getGifticonDetail(nonExistentId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("실패 - 서버 오류")
        void getGifticonDetail_실패_서버오류_test() {
            // given
            String gifticonId = UUID.randomUUID().toString();
            given(gifticonRepository.findById(gifticonId)).willThrow(new RuntimeException("DB 조회 오류"));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> gifticonService.getGifticonDetail(gifticonId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("기프티콘 개수 조회")
    class GetGifticonCount {
        @Test
        @DisplayName("성공")
        void getGifticonCount_성공_test() {
            // given
            long expectedCount = 5L;
            given(gifticonRepository.count()).willReturn(expectedCount);

            // when
            ApiResponse<Long> response = gifticonService.getGifticonCount();

            // then
            assertThat(response.getResultCode()).isEqualTo(200);
            assertThat(response.getData()).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("실패 - 서버 오류")
        void getGifticonCount_실패_서버오류_test() {
            // given
            given(gifticonRepository.count()).willThrow(new RuntimeException("DB 조회 오류"));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> gifticonService.getGifticonCount());
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("기프티콘 교환")
    class ExchangeGifticon {
        @Test
        @DisplayName("성공")
        void exchangeGifticon_성공_test() {
            // given
            String accountId = "test-account-id";
            String gifticonId = "test-gifticon-id";
            String userId = "test-user-id";
            
            Account account = createTestAccount(accountId, 15000L);
            Gifticon gifticon = createTestGifticon(10000L);
            ReflectionTestUtils.setField(gifticon, "id", gifticonId);
            User user = createTestUser(userId, account);

            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(account));
            given(gifticonRepository.findById(gifticonId)).willReturn(Optional.of(gifticon));
            given(userRepository.findByAccountId(accountId)).willReturn(Optional.of(user));

            // when
            ApiResponse<Void> response = gifticonService.exchangeGifticon(gifticonId, accountId);

            // then
            assertThat(response.getResultCode()).isEqualTo(200);
            assertThat(response.getData()).isNull();
            assertThat(account.getMileage()).isEqualTo(5000L); // 15000 - 10000 = 5000
            verify(myGifticonRepository).save(any(UserGifticon.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 계정")
        void exchangeGifticon_실패_계정없음_test() {
            // given
            String nonExistentAccountId = "non-existent-account";
            String gifticonId = "test-gifticon-id";
            
            given(accountRepository.findByIdWithLock(nonExistentAccountId)).willReturn(Optional.empty());

            // when & then
            AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, 
                () -> gifticonService.exchangeGifticon(gifticonId, nonExistentAccountId));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 기프티콘")
        void exchangeGifticon_실패_기프티콘없음_test() {
            // given
            String accountId = "test-account-id";
            String nonExistentGifticonId = "non-existent-gifticon";
            
            Account account = createTestAccount(accountId, 15000L);
            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(account));
            given(gifticonRepository.findById(nonExistentGifticonId)).willReturn(Optional.empty());

            // when & then
            GifticonNotFoundException exception = assertThrows(GifticonNotFoundException.class, 
                () -> gifticonService.exchangeGifticon(nonExistentGifticonId, accountId));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void exchangeGifticon_실패_사용자없음_test() {
            // given
            String accountId = "test-account-id";
            String gifticonId = "test-gifticon-id";
            
            Account account = createTestAccount(accountId, 15000L);
            Gifticon gifticon = createTestGifticon(10000L);
            ReflectionTestUtils.setField(gifticon, "id", gifticonId);
            
            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(account));
            given(gifticonRepository.findById(gifticonId)).willReturn(Optional.of(gifticon));
            given(userRepository.findByAccountId(accountId)).willReturn(Optional.empty());

            // when & then
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
                () -> gifticonService.exchangeGifticon(gifticonId, accountId));
        }

        @Test
        @DisplayName("실패 - 동시 접근으로 인한 락 타임아웃")
        void exchangeGifticon_실패_락타임아웃_test() {
            // given
            String accountId = "test-account-id";
            String gifticonId = "test-gifticon-id";
            
            given(accountRepository.findByIdWithLock(accountId)).willThrow(new PessimisticLockException("Lock timeout"));

            // when & then
            ConcurrentAccessException exception = assertThrows(ConcurrentAccessException.class, 
                () -> gifticonService.exchangeGifticon(gifticonId, accountId));
        }

        @Test
        @DisplayName("실패 - 서버 오류")
        void exchangeGifticon_실패_서버오류_test() {
            // given
            String accountId = "test-account-id";
            String gifticonId = "test-gifticon-id";
            
            Account account = createTestAccount(accountId, 15000L);
            Gifticon gifticon = createTestGifticon(10000L);
            ReflectionTestUtils.setField(gifticon, "id", gifticonId);
            User user = createTestUser("test-user-id", account);
            
            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(account));
            given(gifticonRepository.findById(gifticonId)).willReturn(Optional.of(gifticon));
            given(userRepository.findByAccountId(accountId)).willReturn(Optional.of(user));
            given(myGifticonRepository.save(any(UserGifticon.class))).willThrow(new RuntimeException("DB 저장 오류"));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, 
                () -> gifticonService.exchangeGifticon(gifticonId, accountId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}