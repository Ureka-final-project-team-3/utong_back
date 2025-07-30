package com.ureka.team3.utong_backend.datatrade.dto;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractNotificationDto {
    
    // 구매자 정보
    private String buyerAccountId;
    private String buyerEmail;
    private String buyerNickname;
    private Boolean buyerIsMailEnabled;
    
    // 판매자 정보
    private String sellerAccountId;
    private String sellerEmail;
    private String sellerNickname;
    private Boolean sellerIsMailEnabled;
    
    // 계약 정보
    private String purchaseOrderId;
    private String saleOrderId;
    private String dataCode;
    private Long quantity;
    private Long price;
    private Long totalAmount;
    private LocalDateTime contractedAt;
    
    public static ContractNotificationDto from(Contract contract) {
        Account buyer = contract.getBuyDataRequest().getAccount();
        Account seller = contract.getSaleDataRequest().getAccount();
        
        return ContractNotificationDto.builder()
                .buyerAccountId(buyer.getId())
                .buyerEmail(buyer.getEmail())
                .buyerNickname(buyer.getNickname())
                .buyerIsMailEnabled(buyer.getIsMail())
                
                .sellerAccountId(seller.getId())
                .sellerEmail(seller.getEmail())
                .sellerNickname(seller.getNickname())
                .sellerIsMailEnabled(seller.getIsMail())
                
                .purchaseOrderId(contract.getBuyDataRequest().getId())
                .saleOrderId(contract.getSaleDataRequest().getId())
                .dataCode(contract.getBuyDataRequest().getDataCode())
                .quantity(contract.getAmount())
                .price(contract.getPrice())
                .totalAmount(contract.getPrice() * contract.getAmount())
                .contractedAt(contract.getCreatedAt())
                .build();
    }
    
    // 구매자용 ContractDto 생성
    public ContractDto toBuyerContractDto() {
        return new ContractDto(
                purchaseOrderId,
                saleOrderId,
                buyerAccountId,
                sellerAccountId,
                price,
                quantity,
                dataCode,
                contractedAt
        );
    }
    
    // 판매자용 ContractDto 생성
    public ContractDto toSellerContractDto() {
        return new ContractDto(
                purchaseOrderId,
                saleOrderId,
                buyerAccountId,
                sellerAccountId,
                price,
                quantity,
                dataCode,
                contractedAt
        );
    }
}