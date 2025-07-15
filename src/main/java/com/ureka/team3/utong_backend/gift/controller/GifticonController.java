package com.ureka.team3.utong_backend.gift.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.gift.dto.GifticonResponseDto;
import com.ureka.team3.utong_backend.gift.service.GifticonService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GifticonController {

    private final GifticonService gifticonService;

    @GetMapping("/gifticons")
    public ResponseEntity<ApiResponse<List<GifticonResponseDto>>> getGifticonList() {
        return ResponseEntity.ok(gifticonService.getGifticonList());
    }

    @GetMapping("/gifticons/{gifticonId}")
    public ResponseEntity<ApiResponse<GifticonResponseDto>> getGifticonDetail(@PathVariable("gifticonId") String gifticonId) {
        return ResponseEntity.ok(gifticonService.getGifticonDetail(gifticonId));
    }

    @GetMapping("/gifticons/count")
    public ResponseEntity<ApiResponse<Long>> getGifticonCount() {
        return ResponseEntity.ok(gifticonService.getGifticonCount());
    }

    @PostMapping("/gifticons/{gifticonId}/exchange")
    public ResponseEntity<ApiResponse<Void>> exchangeGifticon(
            @PathVariable("gifticonId") String gifticonId,
            @AuthenticationPrincipal Account account
    ) {
        return ResponseEntity.ok(gifticonService.exchangeGifticon(gifticonId, account.getId()));
    }
}
