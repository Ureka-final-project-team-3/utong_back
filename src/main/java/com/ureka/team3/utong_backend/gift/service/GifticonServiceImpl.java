package com.ureka.team3.utong_backend.gift.service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.gift.dto.GifticonResponseDto;
import com.ureka.team3.utong_backend.gift.repository.GifticonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GifticonServiceImpl implements GifticonService {

    private final GifticonRepository gifticonRepository;

    @Override
    public ApiResponse<List<GifticonResponseDto>> getGifticonList() {
        try {
            List<GifticonResponseDto> list = gifticonRepository.findAll()
                    .stream()
                    .map(GifticonResponseDto::from)
                    .toList();

            return ApiResponse.success(list);
        } catch (Exception e) {
            log.info("기프티콘 목록 조회 중 오류가 발생하였습니다. {}", e.getMessage());
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
            log.info("기프티콘 상세 조회 중 오류가 발생하였습니다. {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<Long> getGifticonCount() {
        try {
            Long count = gifticonRepository.count();

            return ApiResponse.success(count);
        } catch (Exception e) {
            log.info("기프티콘 개수 조회 중 오류가 발생하였습니다. {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
