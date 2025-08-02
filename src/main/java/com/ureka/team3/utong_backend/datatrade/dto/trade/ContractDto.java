package com.ureka.team3.utong_backend.datatrade.dto.trade;

import com.ureka.team3.utong_backend.datatrade.domain.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ContractDto {
    private String purchaseOrderId;

    private String saleOrderId;

    private String purchaseAccountId;

    private String saleAccountId;

    private Long price;

    private Long quantity;

    private String dataCode;

    private LocalDateTime contractedAt;

    public static ContractDto of(Contract contract, String dataCode) {
        ContractDto contractDto = new ContractDto();
        contractDto.purchaseOrderId = contract.getBuyDataRequest().getId();
        contractDto.saleOrderId = contract.getSaleDataRequest().getId();
        contractDto.purchaseAccountId = contract.getBuyDataRequest().getAccount().getId();
        contractDto.saleAccountId = contract.getSaleDataRequest().getAccount().getId();
        contractDto.price = contract.getPrice();
        contractDto.quantity = contract.getAmount();
        contractDto.dataCode = dataCode;  // 파라미터로 받은 값 사용
        contractDto.contractedAt = contract.getCreatedAt();

        return contractDto;
    }

    public static ContractDto of(Contract contract) {
        ContractDto contractDto = new ContractDto();

        contractDto.purchaseOrderId = contract.getBuyDataRequest().getId();
        contractDto.saleOrderId = contract.getSaleDataRequest().getId();
        contractDto.purchaseAccountId = contract.getBuyDataRequest().getAccount().getId();
        contractDto.saleAccountId = contract.getSaleDataRequest().getAccount().getId();
        contractDto.price = contract.getPrice();
        contractDto.quantity = contract.getAmount();
        contractDto.dataCode = contract.getBuyDataRequest().getDataCode();
        contractDto.contractedAt = contract.getCreatedAt();

        return contractDto;
    }

    public static ContractDto ofWithoutAccount(Contract contract) {
        ContractDto contractDto = new ContractDto();
        contractDto.purchaseOrderId = contract.getBuyDataRequest().getId();
        contractDto.saleOrderId = contract.getSaleDataRequest().getId();
        contractDto.price = contract.getPrice();
        contractDto.quantity = contract.getAmount();
        contractDto.dataCode = contract.getBuyDataRequest().getDataCode();
        contractDto.contractedAt = contract.getCreatedAt();

        return contractDto;
    }

}
