package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.PurchaseMatch;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatch;
import com.ureka.team3.utong_backend.datatrade.dto.TradeExecutionDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.BuyDataRequestServiceImpl;
import com.ureka.team3.utong_backend.datatrade.service.trade.contract.ContractService;
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
    public void processBuyMatches(BuyDataRequest buyDataRequest, PurchaseMatch matchOrder) {
        SaleDataRequest saleDataRequest = saleDataRequestService.findSaleOrderById(matchOrder.getMatchedOrder().getOrderId());
        saleDataRequestService.subtractSell(saleDataRequest, matchOrder.getAmount());
        TradeExecutionDto tradeExecutionDto = new TradeExecutionDto(buyDataRequest, saleDataRequest, matchOrder.getAmount(), matchOrder.getPricePerUnit());
        processTrade(tradeExecutionDto);
    }

    @Override
    @Transactional
    public void processSaleMatches(SaleDataRequest request, SaleMatch match) {
        BuyDataRequest buyOrderRequest = buyDataRequestService.findBuyOrderById(match.getMatchedOrder().getOrderId());
        buyDataRequestService.subtractPurchased(buyOrderRequest,match.getAmount());
        TradeExecutionDto tradeExecutionDto = new TradeExecutionDto(buyOrderRequest, request, match.getAmount(), match.getPricePerUnit());
        processTrade(tradeExecutionDto);
    }

    private void processTrade(TradeExecutionDto tradeExecutionDto) {
        // 체결 내용 contract에 저장
        Contract contract = contractService.save(tradeExecutionDto);

        Account account = contract.getBuyDataRequest().getAccount();
        Long totalIncomeForSeller = tradeCalculator.calculateTotalIncomeForSeller(contract.getPrice(), contract.getAmount());

        // 판매자에게 포인트 지급
        pointService.givePoint(account, totalIncomeForSeller);

        String targetLineId = contract.getBuyDataRequest().getLineId();
        // 구매자에게 데이터 지급
        lineService.giveData(targetLineId, contract.getAmount());
    }

}
