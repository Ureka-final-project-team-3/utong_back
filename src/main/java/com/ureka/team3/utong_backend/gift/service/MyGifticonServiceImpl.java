package com.ureka.team3.utong_backend.gift.service;

import com.ureka.team3.utong_backend.common.exception.business.GifticonNotFoundException;
import com.ureka.team3.utong_backend.gift.dto.MyGifticonDetailResponseDto;
import com.ureka.team3.utong_backend.gift.dto.MyGifticonResponseDto;
import com.ureka.team3.utong_backend.gift.entity.UserGifticon;
import com.ureka.team3.utong_backend.gift.repository.MyGifticonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class MyGifticonServiceImpl implements MyGifticonService {

    private final MyGifticonRepository myGifticonRepository;

    @Override
    public List<MyGifticonResponseDto> getMyGifticons(String userId) {
        List<UserGifticon> gifticons = myGifticonRepository.findByUser_Id(userId);

        return gifticons.stream()
                .map(gifticon -> {
                    long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), gifticon.getExpiredAt());
                    String status;
                    if (gifticon.getIsActive()) {
                        status = "사용 완료";
                    } else if (daysRemaining < 0) {
                        status = "유효기간 만료";
                    } else {
                        status = "사용 가능";
                    }

                    return MyGifticonResponseDto.builder()
                            .id(UUID.fromString(gifticon.getId()))
                            .name(gifticon.getGifticon().getName())
                            .description(gifticon.getGifticon().getDescription())
                            .price(gifticon.getGifticon().getPrice())
                            .daysRemaining(daysRemaining)
                            .status(status)
                            .build();
                })
                .collect(toList());
    }

    @Override
    public MyGifticonDetailResponseDto getGifticonDetail(String Id, String userId) {
        UserGifticon gifticon = myGifticonRepository.findByIdAndUser_Id(Id, userId)
                .orElseThrow(GifticonNotFoundException::new);


        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), gifticon.getExpiredAt());
        Boolean active = gifticon.getIsActive();
        String status;
        if (Boolean.TRUE.equals(active)) {
            status = "사용 완료";
        } else if (daysRemaining < 0) {
            status = "유효기간 만료";
        } else {
            status = "사용 가능";
        }

        return MyGifticonDetailResponseDto.builder()
                .id(gifticon.getId())
                .name(gifticon.getGifticon().getName())
                .description(gifticon.getGifticon().getDescription())
                .price(gifticon.getGifticon().getPrice())
                .imageUrl(gifticon.getGifticon().getImageUrl())
                .daysRemaining(daysRemaining)
                .status(status)
                .createdAt(gifticon.getCreatedAt().toString())
                .expiredAt(gifticon.getExpiredAt().toString())
                .build();
    }
}



