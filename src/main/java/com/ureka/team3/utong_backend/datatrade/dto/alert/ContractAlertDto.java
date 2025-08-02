package com.ureka.team3.utong_backend.datatrade.dto.alert;

import com.ureka.team3.utong_backend.datatrade.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractAlertDto {
        RequestType requestType;
        Long price;
        Long quantity;
        String dataCode;
        String orderId;
        LocalDateTime contractedAt;
}
