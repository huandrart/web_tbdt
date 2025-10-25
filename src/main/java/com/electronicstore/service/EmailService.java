package com.electronicstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.verification.expiration}")
    private int verificationExpirationHours;
    
    @Value("${app.email.reset.expiration}")
    private int resetExpirationHours;
    
    public void sendVerificationEmail(String toEmail, String fullName, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Xác thực tài khoản - Electronic Store");
            
            String verificationUrl = "http://localhost:8080/verify-email?token=" + verificationToken;
            
            String emailBody = String.format("""
                Xin chào %s,
                
                Cảm ơn bạn đã đăng ký tài khoản tại Electronic Store!
                
                Để hoàn tất việc đăng ký, vui lòng click vào liên kết bên dưới để xác thực email của bạn:
                
                %s
                
                Liên kết này sẽ hết hạn sau %d giờ.
                
                Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.
                
                Trân trọng,
                Đội ngũ Electronic Store
                """, fullName, verificationUrl, verificationExpirationHours);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Verification email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send verification email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email xác thực", e);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Đặt lại mật khẩu - Electronic Store");
            
            String resetUrl = "http://localhost:8080/reset-password?token=" + resetToken;
            
            String emailBody = String.format("""
                Xin chào %s,
                
                Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Electronic Store.
                
                Để đặt lại mật khẩu, vui lòng click vào liên kết bên dưới:
                
                %s
                
                Liên kết này sẽ hết hạn sau %d giờ.
                
                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                
                Trân trọng,
                Đội ngũ Electronic Store
                """, fullName, resetUrl, resetExpirationHours);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Password reset email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu", e);
        }
    }
}
