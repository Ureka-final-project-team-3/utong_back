package com.ureka.team3.utong_backend.chart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.chart.dto.TradeEventDto;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC = "trade-completed";
    
    public void publishTradeCompleted(String contractId, String dataCode, Long price, Long quantity) {
        try {
            TradeEventDto tradeEvent = TradeEventDto.builder()
                    .contractId(contractId)
                    .dataCode(dataCode)
                    .price(price)
                    .quantity(quantity)
                    .completedAt(LocalDateTime.now())
                    .build();
            
            String message = objectMapper.writeValueAsString(tradeEvent);
            
            kafkaTemplate.send(TOPIC, contractId, message);
            
            log.info("Published trade completion event: contractId={}, dataType={}, price={}", 
                    contractId, tradeEvent.getDataTypeName(), price);
            
        } catch (Exception e) {
            log.error("Failed to publish trade completion event for contract: {}", contractId, e);
        }
    }
    
    public ApiResponse<String> publishMockTradeEvent(String dataType) {
        if (!"LTE".equals(dataType) && !"5G".equals(dataType)) {
            throw new IllegalArgumentException("Invalid data type. Use 'LTE' or '5G'");
        }
        
        String dataCode = "LTE".equals(dataType) ? "001" : "002";
        String contractId = UUID.randomUUID().toString();
        Long basePrice = "LTE".equals(dataType) ? 8000L : 8500L;
        Long price = basePrice + (long)(Math.random() * 1000 - 500);
        Long quantity = (long)(Math.random() * 50 + 1);
        
        publishTradeCompleted(contractId, dataCode, price, quantity);
        
        return ApiResponse.success(
                "Mock trade event published for " + dataType, 
                "Event sent to Kafka topic");
    }
    
    public ApiResponse<String> publishCustomTradeEvent(String dataType, Long price, Long quantity) {
        if (!"LTE".equals(dataType) && !"5G".equals(dataType)) {
            throw new IllegalArgumentException("Invalid data type. Use 'LTE' or '5G'");
        }
        
        String dataCode = "LTE".equals(dataType) ? "001" : "002";
        String contractId = "test-" + System.currentTimeMillis();
        
        publishTradeCompleted(contractId, dataCode, price, quantity);
        
        return ApiResponse.success(
                "Custom trade event published", 
                String.format("DataType: %s, Price: %d, Quantity: %d", dataType, price, quantity));
    }
}