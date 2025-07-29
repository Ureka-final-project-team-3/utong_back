package com.ureka.team3.utong_backend.line.service;

import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.line.repository.LineRepository;
import com.ureka.team3.utong_backend.common.exception.business.LineNotFoundException;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.line.repository.LineDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class LineService {
    private final LineRepository lineRepository;
    private final LineDataRepository lineDataRepository;

    public Line findById(String id) {
        return lineRepository.findById(id).orElseThrow(LineNotFoundException::new);
    }

    @Transactional
    public void giveData(String targetLineId, Long amount) {
        Line line = findById(targetLineId);
        LineData lineData = getLineDataByLineAndDate(line, LocalDate.now());
        lineData.purchaseData(amount);
    }

    @Transactional
    public LineData getLineDataByLineAndDate(Line line, LocalDate date) {
        YearMonth targetMonth = YearMonth.from(date);
        return lineDataRepository.findLineDataByLine(line).stream()
                .filter(ld -> YearMonth.from(ld.getMonth()).equals(targetMonth))
                .findFirst()
                .orElseThrow(() -> new LineNotFoundException("해당 월의 데이터가 없습니다."));
    }

    @Transactional
    public void saleData(String lineId, long dataAmount) {
        Line line = findById(lineId);
        LineData lineData = getLineDataByLineAndDate(line, LocalDate.now());
        lineData.saleData(dataAmount);
    }
}
