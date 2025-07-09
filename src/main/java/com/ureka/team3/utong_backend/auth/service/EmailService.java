package com.ureka.team3.utong_backend.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender javaMailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;
    
    @Value("${mail.enabled:true}")
    private boolean mailEnabled;
    
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String emailContent = createPasswordResetEmailContent(resetUrl);
        
        if (!mailEnabled) {
            logger.info("=== 이메일 전송 시뮬레이션 ===");
            logger.info("받는 사람: {}", toEmail);
            logger.info("보내는 사람: {}", fromEmail);
            logger.info("제목: 비밀번호 재설정 요청");
            logger.info("토큰: {}", token);
            logger.info("재설정 URL: {}", resetUrl);
            logger.info("=========================");
            return;
        }
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "유통 서비스");
            helper.setTo(toEmail);
            helper.setSubject("비밀번호 재설정 요청");
            helper.setText(emailContent, true); 
            
            javaMailSender.send(message);
            logger.info("비밀번호 재설정 이메일 전송 성공: {}", toEmail);
        } catch (Exception e) {
            logger.error("이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage());
        }
    }
    
    private String createPasswordResetEmailContent(String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>비밀번호 재설정</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f8f9fa;
                    }
                    .container {
                        background: white;
                        border-radius: 12px;
                        padding: 40px;
                        box-shadow: 0 4px 20px rgba(0,0,0,0.1);
                        border-top: 4px solid #007bff;
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        color: #007bff;
                        margin-bottom: 10px;
                    }
                    .title {
                        font-size: 24px;
                        color: #2c3e50;
                        margin-bottom: 20px;
                    }
                    .message {
                        font-size: 16px;
                        color: #555;
                        margin-bottom: 30px;
                        text-align: left;
                    }
                    .button-container {
                        text-align: center;
                        margin: 40px 0;
                    }
                    .reset-button {
                        display: inline-block;
                        background: linear-gradient(135deg, #007bff, #0056b3);
                        color: white;
                        text-decoration: none;
                        padding: 16px 40px;
                        border-radius: 8px;
                        font-weight: bold;
                        font-size: 16px;
                        box-shadow: 0 4px 15px rgba(0,123,255,0.3);
                        transition: all 0.3s ease;
                    }
                    .reset-button:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(0,123,255,0.4);
                    }
                    .url-box {
                        background-color: #f8f9fa;
                        border: 1px solid #dee2e6;
                        border-radius: 6px;
                        padding: 15px;
                        margin: 20px 0;
                        word-break: break-all;
                        font-size: 14px;
                        color: #666;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        border-radius: 6px;
                        padding: 15px;
                        margin: 20px 0;
                        font-size: 14px;
                        color: #856404;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px solid #eee;
                        font-size: 14px;
                        color: #666;
                    }
                    .security-info {
                        background-color: #e7f3ff;
                        border-left: 4px solid #007bff;
                        padding: 15px;
                        margin: 20px 0;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">utong</div>
                        <h1 class="title">비밀번호 재설정</h1>
                    </div>
                    
                    <div class="message">
                        <p>안녕하세요,</p>
                        <p>utong 서비스 계정의 비밀번호 재설정 요청을 받았습니다.</p>
                        <p>아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요.</p>
                    </div>
                    
                    <div class="button-container">
                        <a href="%s" class="reset-button">
                            비밀번호 재설정하기
                        </a>
                    </div>
                    
                    <div class="security-info">
                        <strong>보안 안내</strong><br>
                        이 링크는 30분 후에 만료됩니다.<br>
                        보안을 위해 가능한 빨리 비밀번호를 재설정해주세요.
                    </div>
                    
                    <div class="warning">
                        <strong>주의사항</strong><br>
                        만약 비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시하고 삭제해주세요.<br>
                        다른 사람이 귀하의 계정에 접근을 시도할 수 있으니 즉시 고객센터에 문의해주세요.
                    </div>
                    
                    <div class="url-box">
                        <strong>버튼이 작동하지 않는 경우 아래 링크를 복사하여 브라우저에 직접 입력하세요:</strong><br>
                        <a href="%s" style="color: #007bff;">%s</a>
                    </div>
                    
                    <div class="footer">
                        <p>본 메일은 utong 서비스에서 자동으로 발송된 메일입니다.</p>
                        <p>문의사항이 있으시면 고객센터로 연락해주세요.</p>
                        <p style="margin-top: 20px; color: #999;">
                            © 2025 utong 서비스. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, resetUrl, resetUrl, resetUrl);
    }
}