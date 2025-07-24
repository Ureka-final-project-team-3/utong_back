package com.ureka.team3.utong_backend.gift.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.gift.dto.GifticonResponseDto;
import com.ureka.team3.utong_backend.gift.service.GifticonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "기프티콘 교환 API", description = "기프티콘 목록, 상세, 수량 조회 및 교환 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GifticonController {

    private final GifticonService gifticonService;

    @Operation(summary = "기프티콘 목록 조회", description = "전체 기프티콘 목록을 조회합니다.")
    @GetMapping("/gifticons")
    public ResponseEntity<ApiResponse<List<GifticonResponseDto>>> getGifticonList() {
        return ResponseEntity.ok(gifticonService.getGifticonList());
    }

    @Operation(summary = "기프티콘 상세 조회", description = "gifticonId를 통해 특정 기프티콘 정보를 조회합니다.")
    @GetMapping("/gifticons/{gifticonId}")
    public ResponseEntity<ApiResponse<GifticonResponseDto>> getGifticonDetail(@PathVariable("gifticonId") String gifticonId) {
        return ResponseEntity.ok(gifticonService.getGifticonDetail(gifticonId));
    }

    @Operation(summary = "기프티콘 개수 조회", description = "등록된 전체 기프티콘 개수를 조회합니다.")
    @GetMapping("/gifticons/count")
    public ResponseEntity<ApiResponse<Long>> getGifticonCount() {
        return ResponseEntity.ok(gifticonService.getGifticonCount());
    }

    @Operation(
            summary = "기프티콘 교환",
            description = "로그인된 사용자가 해당 gifticonId의 기프티콘을 교환합니다.",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @PostMapping("/gifticons/{gifticonId}/exchange")
    public ResponseEntity<ApiResponse<Void>> exchangeGifticon(
            @PathVariable("gifticonId") String gifticonId,
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.ok(gifticonService.exchangeGifticon(gifticonId, account.getId()));
    }
}
