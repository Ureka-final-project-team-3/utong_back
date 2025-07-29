package com.ureka.team3.utong_backend.datatrade.validator;

import com.ureka.team3.utong_backend.common.exception.business.ExceedSaleLimitException;
import com.ureka.team3.utong_backend.common.exception.business.NotExistDefaultLineException;
import com.ureka.team3.utong_backend.common.exception.business.PriceUnitException;
import com.ureka.team3.utong_backend.common.exception.business.UnlimitedPlanForbiddenTradeException;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
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

    public void validatePurchase(String defaultLineId, DataTradeDto.DataTradeRequestDto dto) {
        if (defaultLineId == null)
            throw new NotExistDefaultLineException();

        Line defaultLine = lineService.findById(defaultLineId);
        if (defaultLine.getPlan().isUnlimited())
            throw new UnlimitedPlanForbiddenTradeException();

        if (!tradeCalculator.isHundredUnit(dto.getPrice()))
            throw new PriceUnitException();

//        if (saleDataRequestRepository.existsWaitingRequestByLineId(defaultLine.getId()))
//            throw new ExistWaitingSaleRequestException();

    }

    public void validateSale(String defaultLineId, DataTradeDto.DataTradeRequestDto dto) {
        if (defaultLineId == null) {
            throw new NotExistDefaultLineException();
        }

        Line defaultLine = lineService.findById(defaultLineId);
        Plan plan = defaultLine.getPlan();


        if (defaultLine.getPlan().isUnlimited())
            throw new UnlimitedPlanForbiddenTradeException();

        LineData lineData = lineService.getLineDataByLineAndDate(defaultLine, LocalDate.now());
        Long canSell = tradeCalculator.calculateCanSellAmount(lineData.getRemaining(), plan.canSell(), lineData.getSell());
        if (dto.getDataAmount() > canSell) {
            throw new ExceedSaleLimitException();
        }

        if (!tradeCalculator.isHundredUnit(dto.getPrice()))
            throw new PriceUnitException();
//        if (buyDataRequestRepository.existsWaitingRequestByLineId(defaultLine.getId())) {
//            throw new ExistWaitingPurchaseRequestException();
//        }
    }
}

