package com.ureka.team3.utong_backend.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossApproveResponse {
    private String paymentKey;
    private String orderId;
    private String status; // DONE


    private Long totalAmount;

    private String requestedAt;
    private String approvedAt;
    private String method;
}
