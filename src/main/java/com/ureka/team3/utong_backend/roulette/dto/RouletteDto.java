package com.ureka.team3.utong_backend.roulette.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RouletteDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfoResponse {
        private String eventId;
        private String title;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer maxWinners;
        private Integer currentWinners;
        private BigDecimal winProbability;
        private Boolean isActive;
        private Boolean canParticipate;
        private Boolean alreadyParticipated;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipateRequest {
        private String eventId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipateResponse {
        private Boolean isWinner;
        private String message;
        private Integer remainingWinners;
        private String eventTitle;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipationHistory {
        private String eventId;
        private String eventTitle;
        private Boolean isWinner;
        private LocalDateTime participatedAt;
    }
}