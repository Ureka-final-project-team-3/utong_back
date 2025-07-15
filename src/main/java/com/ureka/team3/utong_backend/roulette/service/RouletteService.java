package com.ureka.team3.utong_backend.roulette.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.common.exception.business.RouletteAlreadyParticipatedException;
import com.ureka.team3.utong_backend.common.exception.business.RouletteEventNotActiveException;
import com.ureka.team3.utong_backend.common.exception.business.RouletteEventNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.roulette.dto.RouletteDto;
import com.ureka.team3.utong_backend.roulette.entity.RouletteEvent;
import com.ureka.team3.utong_backend.roulette.entity.RouletteParticipation;
import com.ureka.team3.utong_backend.roulette.repository.RouletteEventRepository;
import com.ureka.team3.utong_backend.roulette.repository.RouletteParticipationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouletteService {
    
    private final RouletteEventRepository rouletteEventRepository;
    private final RouletteParticipationRepository participationRepository;
    private final RouletteCouponService rouletteCouponService;
    
    @Transactional(readOnly = true)
    public ApiResponse<RouletteDto.EventInfoResponse> getActiveEventInfo(Account account) {
        try {
            RouletteEvent activeEvent = rouletteEventRepository.findActiveEvent()
                    .orElseThrow(RouletteEventNotFoundException::new);
            
            boolean alreadyParticipated = participationRepository
                    .existsByEventIdAndAccountId(activeEvent.getId(), account.getId());
            
            boolean canParticipate = activeEvent.isEventActive() && 
                                   !alreadyParticipated && 
                                   activeEvent.hasAvailableSlots();
            
            RouletteDto.EventInfoResponse response = RouletteDto.EventInfoResponse.builder()
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
            
            return ApiResponse.success("활성 이벤트 정보를 조회했습니다", response);
            
        } catch (RouletteEventNotFoundException e) {
            return ApiResponse.fail(ErrorCode.ROULETTE_EVENT_NOT_FOUND);
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Transactional
    public ApiResponse<RouletteDto.ParticipateResponse> participate(String eventId, Account account) {
        
        try {
            RouletteEvent event = rouletteEventRepository.findByIdWithLock(eventId)
                    .orElseThrow(RouletteEventNotFoundException::new);
            if (!event.isEventActive()) return ApiResponse.fail(ErrorCode.ROULETTE_EVENT_NOT_ACTIVE);
            
            if (participationRepository.existsByEventIdAndAccountId(eventId, account.getId())) return ApiResponse.fail(ErrorCode.ROULETTE_ALREADY_PARTICIPATED);
            if (!event.hasAvailableSlots()) {
                saveParticipation(event, account, false);
                RouletteDto.ParticipateResponse response = createParticipateResponse(event, false, "당첨자가 모두 마감되었습니다");
                return ApiResponse.success("룰렛 참여가 완료되었습니다", response);
            }
            boolean isWinner = calculateWinProbability(event.getWinProbability());
            if (isWinner) {
                event.incrementWinners();
                rouletteEventRepository.save(event);
                rouletteCouponService.issueWinnerCoupon(account, event);
            }
            saveParticipation(event, account, isWinner);
            String message = isWinner ? "축하합니다! 당첨되셨습니다!" : "아쉽게도 당첨되지 않았습니다";
            RouletteDto.ParticipateResponse response = createParticipateResponse(event, isWinner, message);
            return ApiResponse.success("룰렛 참여가 완료되었습니다", response);
            
        } catch (RouletteEventNotFoundException e) {
            return ApiResponse.fail(ErrorCode.ROULETTE_EVENT_NOT_FOUND);
        } catch (RouletteEventNotActiveException e) {
            return ApiResponse.fail(ErrorCode.ROULETTE_EVENT_NOT_ACTIVE);
        } catch (RouletteAlreadyParticipatedException e) {
            return ApiResponse.fail(ErrorCode.ROULETTE_ALREADY_PARTICIPATED);
        } catch (UserNotFoundException e) {
            return ApiResponse.fail(ErrorCode.USER_NOT_FOUND);
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    private boolean calculateWinProbability(BigDecimal winProbability) {
        double randomValue = Math.random();
        boolean isWinner = randomValue < winProbability.doubleValue();
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