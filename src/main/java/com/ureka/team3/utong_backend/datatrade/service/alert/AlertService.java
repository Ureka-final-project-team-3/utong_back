package com.ureka.team3.utong_backend.datatrade.service.alert;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.alert.ContractAlertDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AlertService {
    void send(String userId, ContractAlertDto alertDto);

    SseEmitter connect(Account account);
}
