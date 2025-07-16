package com.ureka.team3.utong_backend.datatrade.validator;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.line.service.LineService;
import com.ureka.team3.utong_backend.plan.entity.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TradeValidator {
    private final LineService lineService;
    private final SaleDataRequestRepository saleDataRequestRepository;
    private final TradeCalculator tradeCalculator;
    private final BuyDataRequestRepository buyDataRequestRepository;

    public ApiResponse validatePurchase(String defaultLineId) {
        if (defaultLineId == null)
            return TradeResponseFactory.needDefaultLine();

        Line defaultLine = lineService.findById(defaultLineId);
        if (defaultLine.getPlan().getData() == -1)
            return TradeResponseFactory.unlimitedBuyNotAllowed();

        if (saleDataRequestRepository.existsWaitingRequestByLineId(defaultLine.getId()))
            return TradeResponseFactory.existSaleRequest();

        return null;
    }

    public ApiResponse validateSale(String defaultLineId, DataTradeDto.SaleDataRequestDto dto) {
        if (defaultLineId == null) {
            return TradeResponseFactory.needDefaultLine();
        }

        Line defaultLine = lineService.findById(defaultLineId);
        Plan plan = defaultLine.getPlan();
        Long planData = plan.getData();

        if (planData == -1)
            return TradeResponseFactory.unlimitedBuyNotAllowed();

        LineData lineData = lineService.getLineDataByLineAndDate(defaultLine, LocalDate.now());
        Long canSell = tradeCalculator.calculateCanSellAmount(plan.canSell(), lineData.getSell() );
        if (dto.getDataAmount() > canSell) {
            return TradeResponseFactory.exceedSaleLimit();
        }

        if (buyDataRequestRepository.existsWaitingRequestByLineId(defaultLine.getId())) {
            return TradeResponseFactory.existBuyRequest();
        }

        return null;
    }
}
