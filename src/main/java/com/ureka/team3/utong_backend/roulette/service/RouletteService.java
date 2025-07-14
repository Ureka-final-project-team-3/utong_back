package com.ureka.team3.utong_backend.roulette.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.exception.business.*;
import com.ureka.team3.utong_backend.roulette.dto.RouletteDto;
import com.ureka.team3.utong_backend.roulette.entity.RouletteEvent;
import com.ureka.team3.utong_backend.roulette.entity.RouletteParticipation;
import com.ureka.team3.utong_backend.roulette.repository.RouletteEventRepository;
import com.ureka.team3.utong_backend.roulette.repository.RouletteParticipationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouletteService {
    
    private final RouletteEventRepository rouletteEventRepository;
    private final RouletteParticipationRepository participationRepository;
    
    @Transactional(readOnly = true)
    public RouletteDto.EventInfoResponse getActiveEventInfo(Account account) {
        RouletteEvent activeEvent = rouletteEventRepository.findActiveEvent()
                .orElseThrow(RouletteEventNotFoundException::new);
        
        boolean alreadyParticipated = participationRepository
                .existsByEventIdAndAccountId(activeEvent.getId(), account.getId());
        
        boolean canParticipate = activeEvent.isEventActive() && 
                               !alreadyParticipated && 
                               activeEvent.hasAvailableSlots();
        
        return RouletteDto.EventInfoResponse.builder()
                .eventId(activeEvent.getId())
                .title(activeEvent.getTitle())
                .startDate(activeEvent.getStartDate())
                .endDate(activeEvent.getEndDate())
                .maxWinners(activeEvent.getMaxWinners())
                .currentWinners(activeEvent.getCurrentWinners())
                .winProbability(activeEvent.getWinProbability())
                .isActive(activeEvent.isEventActive())
                .canParticipate(canParticipate)
                .alreadyParticipated(alreadyParticipated)
                .build();
    }
    
    @Transactional
    public RouletteDto.ParticipateResponse participate(String eventId, Account account) {
        log.info("룰렛 참여 요청 - EventId: {}, AccountId: {}", eventId, account.getId());
        
        // 1. 비관적 락으로 이벤트 조회
        RouletteEvent event = rouletteEventRepository.findByIdWithLock(eventId)
                .orElseThrow(RouletteEventNotFoundException::new);
        
        // 2. 이벤트 활성 상태 체크
        if (!event.isEventActive()) {
            throw new RouletteEventNotActiveException("이벤트가 종료되었거나 아직 시작되지 않았습니다");
        }
        
        // 3. 중복 참여 체크
        if (participationRepository.existsByEventIdAndAccountId(eventId, account.getId())) {
            throw new RouletteAlreadyParticipatedException("이미 참여한 이벤트입니다");
        }
        
        // 4. 당첨자 수 제한 체크
        if (!event.hasAvailableSlots()) {
            // 꽝으로 참여 기록만 저장
            saveParticipation(event, account, false);
            return createParticipateResponse(event, false, "당첨자가 모두 마감되었습니다");
        }
        
        // 5. 확률 계산
        boolean isWinner = calculateWinProbability(event.getWinProbability());
        
        // 6. 당첨이면 당첨자 수 증가
        if (isWinner) {
            event.incrementWinners();
            rouletteEventRepository.save(event);
            log.info("당첨 처리 완료 - 현재 당첨자 수: {}/{}", event.getCurrentWinners(), event.getMaxWinners());
        }
        
        // 7. 참여 기록 저장
        saveParticipation(event, account, isWinner);
        
        String message = isWinner ? "축하합니다! 당첨되셨습니다!" : "아쉽게도 당첨되지 않았습니다";
        return createParticipateResponse(event, isWinner, message);
    }
    
    private boolean calculateWinProbability(BigDecimal winProbability) {
        double randomValue = Math.random();
        boolean isWinner = randomValue < winProbability.doubleValue();
        
        log.info("확률 계산 - 설정 확률: {}, 랜덤값: {}, 당첨 여부: {}", 
                winProbability, randomValue, isWinner);
        
        return isWinner;
    }
    
    private void saveParticipation(RouletteEvent event, Account account, boolean isWinner) {
        RouletteParticipation participation = RouletteParticipation.builder()
                .id(UUID.randomUUID().toString())
                .event(event)
                .account(account)
                .isWinner(isWinner)
                .build();
        
        participationRepository.save(participation);
        log.info("참여 기록 저장 완료 - AccountId: {}, Winner: {}", account.getId(), isWinner);
    }
    
    private RouletteDto.ParticipateResponse createParticipateResponse(
            RouletteEvent event, boolean isWinner, String message) {
        
        int remainingWinners = Math.max(0, event.getMaxWinners() - event.getCurrentWinners());
        
        return RouletteDto.ParticipateResponse.builder()
                .isWinner(isWinner)
                .message(message)
                .remainingWinners(remainingWinners)
                .eventTitle(event.getTitle())
                .build();
    }
}