package com.ureka.team3.utong_backend.datatrade.messaging.publisher;

import com.ureka.team3.utong_backend.config.RabbitMQConfig;
import com.ureka.team3.utong_backend.datatrade.messaging.message.TradeExecutedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEmailNotification(TradeExecutedMessage message) {
        try {
            if(message.getNewContracts() == null || message.getNewContracts().isEmpty()) {
                return;
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    message
            );

            log.info("이메일 알림 메시지 RabbitMQ 발행 - 데이터코드 : {}, 계약수: {}",
                    message.getDataCode(), message.getNewContracts().size());
        } catch (Exception e) {
            log.error("이메일 알림 메시지 RabbitMQ 발행 실패 - 오류 : {}", e.getMessage());
        }
    }
}
