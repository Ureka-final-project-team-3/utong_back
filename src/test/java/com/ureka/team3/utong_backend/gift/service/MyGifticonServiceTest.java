package com.ureka.team3.utong_backend.gift.service;


import com.ureka.team3.utong_backend.gift.dto.MyGifticonDetailResponseDto;
import com.ureka.team3.utong_backend.gift.entity.Gifticon;
import com.ureka.team3.utong_backend.gift.entity.UserGifticon;
import com.ureka.team3.utong_backend.gift.repository.MyGifticonRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MyGifticonServiceTest {

    private final MyGifticonRepository myGifticonRepository = mock(MyGifticonRepository.class);
    private final MyGifticonServiceImpl service = new MyGifticonServiceImpl(myGifticonRepository);

    @Test
    void getMyGifticons_정상_리턴() {
        // ✅ 유효한 UUID 형식의 문자열 사용
        String uuid = UUID.randomUUID().toString();

        UserGifticon mockGifticon = UserGifticon.builder()
                .id(uuid)
                .gifticon(Gifticon.builder()
                        .name("스타벅스")
                        .description("아메리카노 T")
                        .price(5000L)
                        .build())
                .isActive(false)
                .expiredAt(LocalDateTime.now().plusDays(3))
                .build();

        when(myGifticonRepository.findByUser_Id("user-1"))
                .thenReturn(Collections.singletonList(mockGifticon));

        var result = service.getMyGifticons("user-1");

        // ✅ 결과 검증
        assertEquals(1, result.size());
        assertEquals("스타벅스", result.get(0).getName());
        assertEquals("사용 가능", result.get(0).getStatus());
        assertEquals(UUID.fromString(uuid), result.get(0).getId());
    }

    @Test
    void getGifticonDetail_정상_리턴() {
        String uuid = UUID.randomUUID().toString();

        UserGifticon mockGifticon = UserGifticon.builder()
                .id(uuid)
                .gifticon(Gifticon.builder()
                        .name("배라")
                        .description("파인트")
                        .price(15000L)
                        .imageUrl("https://img.url")
                        .build())
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(5))
                .expiredAt(LocalDateTime.now().plusDays(10))
                .build();

        when(myGifticonRepository.findByIdAndUser_Id(uuid, "user-1"))
                .thenReturn(Optional.of(mockGifticon));

        MyGifticonDetailResponseDto result = service.getGifticonDetail(uuid, "user-1");

        assertEquals("배라", result.getName());
        assertEquals("사용 가능", result.getStatus());
        assertTrue(result.getDaysRemaining() > 0);
        assertEquals(uuid, result.getId());
    }
}