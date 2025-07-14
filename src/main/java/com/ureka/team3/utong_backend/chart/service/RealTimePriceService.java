package com.ureka.team3.utong_backend.chart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.chart.dto.PriceDataDto;
import com.ureka.team3.utong_backend.chart.dto.TradeEventDto;
import com.ureka.team3.utong_backend.chart.dto.TradePriceResponse;
import com.ureka.team3.utong_backend.chart.repository.PriceDataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimePriceService {
    
    private final PriceDataRepository priceDataRepository;
    private final ObjectMapper objectMapper;
    
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public SseEmitter subscribeToPriceUpdates(String dataType) {
        if (!"LTE".equals(dataType) && !"5G".equals(dataType)) {
            throw new IllegalArgumentException("Invalid data type. Use 'LTE' or '5G'");
        }
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitters.computeIfAbsent(dataType, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        emitter.onCompletion(() -> removeEmitter(dataType, emitter));
        emitter.onTimeout(() -> removeEmitter(dataType, emitter));
        emitter.onError(e -> removeEmitter(dataType, emitter));
        
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to " + dataType + " price updates"));
                    
            sendInitialData(emitter, dataType);
        } catch (IOException e) {
            log.error("Error sending initial connection message", e);
            removeEmitter(dataType, emitter);
        }
        
        return emitter;
    }
    
    private void sendInitialData(SseEmitter emitter, String dataType) {
        try {
            List<PriceDataDto> initialData = getRecentPriceData(dataType);
            TradePriceResponse response = TradePriceResponse.of(dataType, initialData);
            
            emitter.send(SseEmitter.event()
                    .name("price-data")
                    .data(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            log.error("Error sending initial data for " + dataType, e);
        }
    }
    
    public void broadcastTradeUpdate(TradeEventDto tradeEvent) {
        String dataType = tradeEvent.getDataTypeName();
        List<SseEmitter> typeEmitters = emitters.get(dataType);
        
        if (typeEmitters == null || typeEmitters.isEmpty()) {
            return;
        }
        
        try {
            List<PriceDataDto> updatedData = getRecentPriceData(dataType);
            TradePriceResponse response = TradePriceResponse.of(dataType, updatedData);
            
            String jsonData = objectMapper.writeValueAsString(response);
            
            typeEmitters.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("price-update")
                            .data(jsonData));
                    return false;
                } catch (IOException e) {
                    log.warn("Failed to send price update, removing emitter", e);
                    return true;
                }
            });
            
            log.info("Broadcasted price update for {}: {} connections", dataType, typeEmitters.size());
            
        } catch (Exception e) {
            log.error("Error broadcasting trade update for " + dataType, e);
        }
    }
    
    private List<PriceDataDto> getRecentPriceData(String dataType) {
        String dataCode = "LTE".equals(dataType) ? "001" : "002";
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        
        try {
            List<Object[]> rawData = priceDataRepository.findRecentPriceData(dataCode, startTime);
            List<PriceDataDto> priceData = new ArrayList<>();
            
            for (Object[] row : rawData) {
                String timestamp = (String) row[0];
                Long price = ((Number) row[1]).longValue();
                Integer volume = ((Number) row[2]).intValue();
                
                priceData.add(PriceDataDto.builder()
                        .timestamp(timestamp)
                        .price(price)
                        .volume(volume)
                        .build());
            }
            
            return priceData;
        } catch (Exception e) {
            log.error("Error fetching recent price data for " + dataType, e);
            return generateMockData(dataType);
        }
    }
    
    private List<PriceDataDto> generateMockData(String dataType) {
        List<PriceDataDto> mockData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 19; i >= 0; i--) {
            LocalDateTime timestamp = now.minusMinutes(i);
            Long basePrice = "LTE".equals(dataType) ? 8000L : 8500L;
            Long price = basePrice + (long)(Math.random() * 1000 - 500);
            Integer volume = (int)(Math.random() * 50 + 1);
            
            mockData.add(PriceDataDto.of(timestamp, price, volume));
        }
        
        return mockData;
    }
    
    private void removeEmitter(String dataType, SseEmitter emitter) {
        List<SseEmitter> typeEmitters = emitters.get(dataType);
        if (typeEmitters != null) {
            typeEmitters.remove(emitter);
            log.debug("Removed emitter for {}, remaining: {}", dataType, typeEmitters.size());
        }
    }
    
    public Map<String, Integer> getConnectionCounts() {
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        emitters.forEach((dataType, emitterList) -> 
                counts.put(dataType, emitterList.size()));
        return counts;
    }
}