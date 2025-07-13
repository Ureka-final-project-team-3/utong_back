package com.ureka.team3.utong_backend.gift.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.gift.dto.GifticonDetailRequestDto;
import com.ureka.team3.utong_backend.gift.dto.MyGifticonDetailResponseDto;
import com.ureka.team3.utong_backend.gift.dto.MyGifticonResponseDto;
import com.ureka.team3.utong_backend.gift.service.MyGifticonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/gifticons")
//@Tag(name = "마이페이지 - 기프티콘", description = "마이페이지에서 내 기프티콘 목록을 조회하는 API")
public class MyGifticonController {

    private final MyGifticonService mypageGifticonService;

    @GetMapping
//    @Operation(summary = "내 기프티콘 목록 조회",description = "로그인된 사용자의 기프티콘 목록을 반환합니다.")
    public ResponseEntity<ApiResponse<List<MyGifticonResponseDto>>> getMyGifticons(@AuthenticationPrincipal Account account) {
        String userId = account.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(mypageGifticonService.getMyGifticons(userId)));
    }

    @PostMapping("/detail")
//      @Operation(summary = "기프티콘 상세 조회", description = "기프티콘 ID를 JSON으로 받아 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<MyGifticonDetailResponseDto>> getGifticonDetail(
            @AuthenticationPrincipal Account account,
            @RequestBody GifticonDetailRequestDto request
    ) {
        String userId = account.getUser().getId();
        String gifticonId = request.getGifticonId();
        MyGifticonDetailResponseDto detail = mypageGifticonService.getGifticonDetail(gifticonId, userId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

}

