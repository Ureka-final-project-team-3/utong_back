package com.ureka.team3.utong_backend.gift.dto;

import com.ureka.team3.utong_backend.gift.entity.Gifticon;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GifticonResponseDto {

    private String id;

    private Long price;

    private String description;

    private String name;

    private String imageUrl;

    private String imageKey;

    private String category;
    
    public static GifticonResponseDto from(Gifticon gifticon) {
        GifticonResponseDto gifticonResponseDto = new GifticonResponseDto();
        
        gifticonResponseDto.id = gifticon.getId();
        gifticonResponseDto.price = gifticon.getPrice();
        gifticonResponseDto.description = gifticon.getDescription();
        gifticonResponseDto.name = gifticon.getName();
        gifticonResponseDto.imageUrl = gifticon.getImageUrl();
        gifticonResponseDto.imageKey = gifticon.getImageKey();
        gifticonResponseDto.category = gifticon.getCategory();
        return gifticonResponseDto;
    }
}
