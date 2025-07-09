package com.ureka.team3.utong_backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final JavaMailSender javaMailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;
    
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("유통 서비스 <" + fromEmail + ">");
            message.setTo(toEmail);
            message.setSubject("비밀번호 재설정 요청");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String emailContent = createPasswordResetEmailContent(resetUrl);
            
            message.setText(emailContent);
            
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage());
        }
    }
    
    private String createPasswordResetEmailContent(String resetUrl) {
        return String.format("""
            안녕하세요,
            
            비밀번호 재설정 요청을 받았습니다.
            
            아래 링크를 클릭하여 새로운 비밀번호를 설정해주세요.
            (이 링크는 30분 후에 만료됩니다)
            
            %s
            
            만약 비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시해주세요.
            
            감사합니다.
            """, resetUrl);
    }
}