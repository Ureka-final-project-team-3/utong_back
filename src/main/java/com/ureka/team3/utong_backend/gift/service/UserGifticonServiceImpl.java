package com.ureka.team3.utong_backend.gift.service;

import com.ureka.team3.utong_backend.common.exception.business.GifticonNotFoundException;
import com.ureka.team3.utong_backend.gift.config.GifticonPolicy;
import com.ureka.team3.utong_backend.gift.dto.UserGifticonDetailResponseDto;
import com.ureka.team3.utong_backend.gift.dto.UserGifticonResponseDto;
import com.ureka.team3.utong_backend.gift.entity.UserGifticon;
import com.ureka.team3.utong_backend.gift.enums.GifticonStatus;
import com.ureka.team3.utong_backend.gift.repository.UserGifticonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserGifticonServiceImpl implements UserGifticonService {

    private final UserGifticonRepository myGifticonRepository;
    private final GifticonPolicy gifticonPolicy;

    @Override
    public List<UserGifticonResponseDto> getMyGifticons(String userId) {
        List<UserGifticon> gifticons = myGifticonRepository.findByUser_Id(userId);

        return gifticons.stream()
                .map(gifticon -> {
                    long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), gifticon.getExpiredAt());
                    GifticonStatus status = (daysRemaining < 0 ) ? GifticonStatus.EXPIRED : GifticonStatus.AVAILABLE;

                    return UserGifticonResponseDto.builder()
                            .id(UUID.fromString(gifticon.getId()))
                            .name(gifticon.getGifticon().getName())
                            .description(gifticon.getGifticon().getDescription())
                            .price(gifticon.getGifticon().getPrice())
                            .imageUrl(gifticon.getGifticon().getImageUrl())
                            .daysRemaining(daysRemaining)
                            .status(gifticonPolicy.getStatusCodeByName(status.name()).getCodeNameBrief())
                            .build();
                })
                .collect(toList());
    }

    @Override
    public UserGifticonDetailResponseDto getGifticonDetail(String Id, String userId) {
        UserGifticon gifticon = myGifticonRepository.findByIdAndUser_Id(Id, userId)
                .orElseThrow(GifticonNotFoundException::new);


        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), gifticon.getExpiredAt());
        GifticonStatus status = (daysRemaining < 0 ) ? GifticonStatus.EXPIRED : GifticonStatus.AVAILABLE;

        return UserGifticonDetailResponseDto.builder()
                .id(gifticon.getId())
                .name(gifticon.getGifticon().getName())
                .description(gifticon.getGifticon().getDescription())
                .price(gifticon.getGifticon().getPrice())
                .imageUrl(gifticon.getGifticon().getImageUrl())
                .daysRemaining(daysRemaining)
                .status(gifticonPolicy.getStatusCodeByName(status.name()).getCodeNameBrief())
                .createdAt(gifticon.getCreatedAt().toString())
                .expiredAt(gifticon.getExpiredAt().toString())
                .build();
    }
}



