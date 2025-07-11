//package com.ureka.team3.utong_backend.chart.listener;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ureka.team3.utong_backend.trade.dto.TradeEventDto;
//import com.ureka.team3.utong_backend.trade.service.RealTimePriceService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class TradeEventListener {
//    
//    private final RealTimePriceService realTimePriceService;
//    private final ObjectMapper objectMapper;
//    
//    @KafkaListener(topics = "trade-completed", groupId = "price-update-group")
//    public void handleTradeCompleted(String message) {
//        try {
//            log.info("Received trade completion event: {}", message);
//            
//            TradeEventDto tradeEvent = objectMapper.readValue(message, TradeEventDto.class);
//            
//            log.info("Processing trade: Contract ID={}, DataType={}, Price={}, Quantity={}", 
//                    tradeEvent.getContractId(), 
//                    tradeEvent.getDataTypeName(), 
//                    tradeEvent.getPrice(), 
//                    tradeEvent.getQuantity());
//            
//            realTimePriceService.broadcastTradeUpdate(tradeEvent);
//            
//        } catch (Exception e) {
//            log.error("Error processing trade completion event: {}", message, e);
//        }
//    }
//}

