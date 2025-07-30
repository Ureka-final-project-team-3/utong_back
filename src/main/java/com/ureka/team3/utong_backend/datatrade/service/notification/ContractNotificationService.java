package com.ureka.team3.utong_backend.datatrade.service.notification;

import com.ureka.team3.utong_backend.datatrade.alert.AlertService;
import com.ureka.team3.utong_backend.datatrade.dto.ContractAlertDto;
import com.ureka.team3.utong_backend.datatrade.dto.ContractDto;
import com.ureka.team3.utong_backend.datatrade.dto.ContractNotificationDto;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractNotificationService {

    private final JavaMailSender javaMailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url:https://utong.site}")
    private String frontendUrl;
    
    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    public void sendContractNotification(Contract contract) {
        ContractNotificationDto notificationDto = ContractNotificationDto.from(contract);
        
        sendBuyerNotifications(notificationDto);
        sendSellerNotifications(notificationDto);
    }
    
    private void sendBuyerNotifications(ContractNotificationDto dto) {

        if (Boolean.TRUE.equals(dto.getBuyerIsMailEnabled())) {
            sendBuyerEmail(dto);
            log.info("구매자 이메일 알림 전송 - 사용자: {}, 이메일: {}", 
                    dto.getBuyerAccountId(), dto.getBuyerEmail());
        } else {
            log.debug("구매자 이메일 알림 스킵 - 사용자: {}, IS_MAIL: {}", 
                    dto.getBuyerAccountId(), dto.getBuyerIsMailEnabled());
        }
    }
    
    private void sendSellerNotifications(ContractNotificationDto dto) {
        
        if (Boolean.TRUE.equals(dto.getSellerIsMailEnabled())) {
            sendSellerEmail(dto);
            log.info("판매자 이메일 알림 전송 - 사용자: {}, 이메일: {}", 
                    dto.getSellerAccountId(), dto.getSellerEmail());
        } else {
            log.debug("판매자 이메일 알림 스킵 - 사용자: {}, IS_MAIL: {}", 
                    dto.getSellerAccountId(), dto.getSellerIsMailEnabled());
        }
    }

    private void sendBuyerEmail(ContractNotificationDto dto) {
        String subject = "데이터 구매 계약 체결 알림";
        String content = createBuyerEmailContent(dto);
        
        sendEmail(dto.getBuyerEmail(), subject, content, "구매자");
    }

    private void sendSellerEmail(ContractNotificationDto dto) {
        String subject = "데이터 판매 계약 체결 알림";
        String content = createSellerEmailContent(dto);
        
        sendEmail(dto.getSellerEmail(), subject, content, "판매자");
    }

    private void sendEmail(String toEmail, String subject, String content, String userType) {
        if (!mailEnabled) {
            log.info("=== 계약 이메일 전송 시뮬레이션 ===");
            log.info("받는 사람: {} ({})", toEmail, userType);
            log.info("보내는 사람: {}", fromEmail);
            log.info("제목: {}", subject);
            log.info("===========================");
            return;
        }
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            javaMailSender.send(message);
            log.info("{} 계약 알림 이메일 전송 완료: {}", userType, toEmail);
            
        } catch (MessagingException e) {
            log.error("{} 계약 알림 이메일 전송 실패: {}, 오류: {}", userType, toEmail, e.getMessage());
        }
    }

    private String createBuyerEmailContent(ContractNotificationDto dto) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>데이터 구매 계약 체결 알림</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .info-box { background-color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                    .button { background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>데이터 구매 계약 체결 완료</h1>
                    </div>
                    <div class="content">
                        <p>안녕하세요, %s님!</p>
                        <p>요청하신 데이터 구매 계약이 성공적으로 체결되었습니다.</p>
                        
                        <div class="info-box">
                            <h3>계약 정보</h3>
                            <ul>
                                <li><strong>구매 주문 ID:</strong> %s</li>
                                <li><strong>판매 주문 ID:</strong> %s</li>
                                <li><strong>데이터 코드:</strong> %s</li>
                                <li><strong>구매 수량:</strong> %,dGB</li>
                                <li><strong>단가:</strong> %,dp</li>
                                <li><strong>총 결제 금액:</strong> %,dp</li>
                                <li><strong>계약 일시:</strong> %s</li>
                            </ul>
                        </div>
                        
                        <p>구매하신 데이터는 즉시 사용 가능합니다.</p>
                    </div>
                    <div class="footer">
                        <p>문의사항이 있으시면 언제든 연락해 주세요.</p>
                        <p>우통(UTONG) 팀 드림</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            dto.getBuyerNickname(),
            dto.getPurchaseOrderId(),
            dto.getSaleOrderId(),
            convertDataCode(dto.getDataCode()),
            dto.getQuantity(),
            dto.getPrice(),
            dto.getTotalAmount(),
            dto.getContractedAt().toString()
        );
    }

    private String createSellerEmailContent(ContractNotificationDto dto) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>데이터 판매 계약 체결 알림</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .info-box { background-color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                    .button { background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 10px 0; }
                    .highlight { color: #28a745; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>데이터 판매 계약 체결 완료</h1>
                    </div>
                    <div class="content">
                        <p>안녕하세요, %s님!</p>
                        <p>판매 요청하신 데이터가 성공적으로 판매되었습니다.</p>
                        
                        <div class="info-box">
                            <h3>계약 정보</h3>
                            <ul>
                                <li><strong>판매 주문 ID:</strong> %s</li>
                                <li><strong>구매 주문 ID:</strong> %s</li>
                                <li><strong>데이터 코드:</strong> %s</li>
                                <li><strong>판매 수량:</strong> %,dGB</li>
                                <li><strong>단가:</strong> %,dp</li>
                                <li><strong>총 판매 금액:</strong> <span class="highlight">%,dp</span></li>
                                <li><strong>계약 일시:</strong> %s</li>
                            </ul>
                        </div>
                        
                        <p>판매 수익이 즉시 계정에 적립되었습니다.</p>
                    </div>
                    <div class="footer">
                        <p>지속적인 데이터 판매를 통해 더 많은 수익을 얻어보세요!</p>
                        <p>유통(UTONG) 팀 드림</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            dto.getSellerNickname(),
            dto.getSaleOrderId(),
            dto.getPurchaseOrderId(),
            convertDataCode(dto.getDataCode()),
            dto.getQuantity(),
            dto.getPrice(),
            dto.getTotalAmount(),
            dto.getContractedAt().toString()
        );
    }
    public String convertDataCode(String s)
    {
    	if(s.equals("001")) return "LTE";
    	else return "5G";
    }
}