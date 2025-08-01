package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.trade.ContractDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.TradeExecutionDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.service.trade.contract.ContractService;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.BuyDataRequestServiceImpl;
import com.ureka.team3.utong_backend.datatrade.service.trade.sale.SaleDataRequestService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.line.service.LineService;
import com.ureka.team3.utong_backend.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeProcessorImpl implements TradeProcessor {
    private final BuyDataRequestServiceImpl buyDataRequestService;
    private final SaleDataRequestService saleDataRequestService;
    private final ContractService contractService;
    private final TradeCalculator tradeCalculator;
    private final PointService pointService;
    private final LineService lineService;

    @Override
    @Transactional
    public ContractDto processBuyMatches(BuyDataRequest buyDataRequest, TradeMatch matchOrder) {
        SaleDataRequest saleDataRequest = saleDataRequestService.findSaleOrderById(matchOrder.getMatchedOrder().getOrderId());
        saleDataRequest.subtractRemain(matchOrder.getAmount());
        buyDataRequest.subtractRemain(matchOrder.getAmount());
        TradeExecutionDto tradeExecutionDto = new TradeExecutionDto(buyDataRequest, saleDataRequest, matchOrder.getAmount(), matchOrder.getPricePerUnit());
        return processTrade(tradeExecutionDto);
    }

    @Override
    @Transactional
    public ContractDto processSaleMatches(SaleDataRequest saleDataRequest, TradeMatch matchOrder) {
        BuyDataRequest buyDataRequest = buyDataRequestService.findBuyOrderById(matchOrder.getMatchedOrder().getOrderId());
        saleDataRequest.subtractRemain(matchOrder.getAmount());
        buyDataRequest.subtractRemain(matchOrder.getAmount());
        TradeExecutionDto tradeExecutionDto = new TradeExecutionDto(buyDataRequest, saleDataRequest, matchOrder.getAmount(), matchOrder.getPricePerUnit());
        return processTrade(tradeExecutionDto);
    }

    private ContractDto processTrade(TradeExecutionDto tradeExecutionDto) {
        // 체결 내용 contract에 저장
        Contract contract = contractService.save(tradeExecutionDto);

        Account account = contract.getSaleDataRequest().getAccount();
        Long totalIncomeForSeller = tradeCalculator.calculateTotalIncomeForSeller(contract.getPrice(), contract.getAmount());

        // 판매자에게 포인트 지급
        pointService.givePoint(account, totalIncomeForSeller);

        String targetLineId = contract.getBuyDataRequest().getLineId();
        // 구매자에게 데이터 지급
        lineService.giveData(targetLineId, contract.getAmount());
        return ContractDto.of(contract);

    }

}
