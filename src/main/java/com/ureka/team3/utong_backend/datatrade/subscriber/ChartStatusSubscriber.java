package com.ureka.team3.utong_backend.datatrade.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.AggregationStatusMessage;
import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.dto.ChartDataDto;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChartStatusSubscriber implements MessageListener {   // 평균 시세 그래프 집계 완료 시 호출 (1시간 마다)

    private final @Qualifier("chartSseHandler")SseHandler<ChartDataDto> sseHandler;
    private final ObjectMapper objectMapper;
    private final DataTradePolicy dataTradePolicy;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("집계 완료 메시지 수신: {}", message);

            AggregationStatusMessage aggregationStatusMessage = getAggregationStatusMessage(message);
            if ("SUCCESS".equals(aggregationStatusMessage.getStatus())) {
//                requestBroadCast(aggregationStatusMessage);
                requestAllBroadCast(aggregationStatusMessage);
            } else {
                log.warn("집계 실패 알림 수신: {}", aggregationStatusMessage.getMessage());
            }
        } catch (Exception e) {
            log.error("집계 완료 메시지 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private AggregationStatusMessage getAggregationStatusMessage(Message message) throws JsonProcessingException {
        String payload = new String(message.getBody());
        return getAggregationStatusMessage(payload);
    }

    private AggregationStatusMessage getAggregationStatusMessage(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, AggregationStatusMessage.class);
    }

    private void requestBroadCast(AggregationStatusMessage aggregationStatusMessage) {
        Map<String, List<AvgPerHour>> dataMap = aggregationStatusMessage.getDataMap();

        for (Code code : dataTradePolicy.getDataTypeCodeList()) {
            List<AvgPerHour> avgPerHourList = dataMap.get(code.getCode());
            if (avgPerHourList != null) {
                ChartDataDto chartDataDto = ChartDataDto.builder()
                        .dataCode(code.getCode())
                        .avgPerHourList(avgPerHourList)
                        .build();

                sseHandler.broadCast(code.getCode(), chartDataDto);
            }
        }
    }

    private void requestAllBroadCast(AggregationStatusMessage aggregationStatusMessage) {
        log.info("집계 완료 처리 성공: {}", aggregationStatusMessage.getAggregatedAt());
        Map<String, List<AvgPerHour>> dataMap = aggregationStatusMessage.getDataMap();

        // 모든 데이터를 통합해서 브로드캐스트
        List<ChartDataDto> allChartData = new ArrayList<>();

        for (Code code : dataTradePolicy.getDataTypeCodeList()) {
            List<AvgPerHour> avgPerHourList = dataMap.get(code.getCode());
            if (avgPerHourList != null) {
                avgPerHourList.forEach(avgPerHour -> {
                    if (avgPerHour.getDataCode() == null) {
                        avgPerHour.setDataCode(code.getCode());
                    }
                });

                ChartDataDto chartDataDto = ChartDataDto.builder()
                        .dataCode(code.getCode())
                        .avgPerHourList(avgPerHourList)
                        .build();

                allChartData.add(chartDataDto);
            } else {
                log.warn("집계 데이터가 없습니다. [code: {}]", code.getCode());
            }
        }

        // 통합 데이터 브로드캐스트
        sseHandler.broadCastAllData(allChartData);
    }


}
