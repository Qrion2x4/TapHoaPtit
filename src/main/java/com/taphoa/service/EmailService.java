package com.taphoa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${app.name}")
    private String appName;
    
    /**
     * Gửi email xác thực tài khoản
     */
    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Xác thực tài khoản " + appName);
            
            String verificationLink = baseUrl + "/verify-email?token=" + token;
            
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #4CAF50; text-align: center;">🎉 Chào mừng đến với %s!</h2>
                    
                    <p>Xin chào <strong>%s</strong>,</p>
                    
                    <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>%s</strong>!</p>
                    
                    <p>Vui lòng click vào nút bên dưới để xác thực địa chỉ email của bạn:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            ✅ Xác thực email
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px;">Hoặc copy link sau vào trình duyệt:</p>
                    <p style="background-color: #f5f5f5; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 12px;">
                        %s
                    </p>
                    
                    <p style="color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #ddd; padding-top: 20px;">
                        ⚠️ Link này sẽ hết hạn sau 24 giờ.<br>
                        ⚠️ Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.
                    </p>
                    
                    <p style="text-align: center; color: #999; font-size: 12px; margin-top: 20px;">
                        © 2025 %s. All rights reserved.
                    </p>
                </div>
                """.formatted(appName, username, appName, verificationLink, verificationLink, appName);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Email xác thực đã gửi đến: " + toEmail);
            
        } catch (MessagingException e) {
            System.err.println("❌ Lỗi gửi email xác thực: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email xác thực. Vui lòng thử lại sau!");
        }
    }
    
    /**
     * Gửi email reset mật khẩu
     */
    public void sendResetPasswordEmail(String toEmail, String username, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔒 Yêu cầu đặt lại mật khẩu - " + appName);
            
            String resetLink = baseUrl + "/reset-password?token=" + token;
            
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #FF9800; text-align: center;">🔒 Đặt lại mật khẩu</h2>
                    
                    <p>Xin chào <strong>%s</strong>,</p>
                    
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại <strong>%s</strong>.</p>
                    
                    <p>Click vào nút bên dưới để đặt lại mật khẩu:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #FF9800; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            🔑 Đặt lại mật khẩu
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px;">Hoặc copy link sau vào trình duyệt:</p>
                    <p style="background-color: #f5f5f5; padding: 10px; border-radius: 5px; word-break: break-all; font-size: 12px;">
                        %s
                    </p>
                    
                    <p style="color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #ddd; padding-top: 20px;">
                        ⚠️ Link này sẽ hết hạn sau 1 giờ.<br>
                        ⚠️ Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và mật khẩu của bạn sẽ không bị thay đổi.
                    </p>
                    
                    <p style="text-align: center; color: #999; font-size: 12px; margin-top: 20px;">
                        © 2025 %s. All rights reserved.
                    </p>
                </div>
                """.formatted(username, appName, resetLink, resetLink, appName);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Email reset password đã gửi đến: " + toEmail);
            
        } catch (MessagingException e) {
            System.err.println("❌ Lỗi gửi email reset password: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau!");
        }
    }
    
    /**
     * Gửi email thông báo đổi mật khẩu thành công
     */
    public void sendPasswordChangedEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("✅ Mật khẩu đã được thay đổi - " + appName);
            
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #4CAF50; text-align: center;">✅ Mật khẩu đã được thay đổi</h2>
                    
                    <p>Xin chào <strong>%s</strong>,</p>
                    
                    <p>Mật khẩu cho tài khoản của bạn tại <strong>%s</strong> đã được thay đổi thành công.</p>
                    
                    <p>Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với chúng tôi ngay lập tức!</p>
                    
                    <p style="text-align: center; color: #999; font-size: 12px; margin-top: 30px;">
                        © 2025 %s. All rights reserved.
                    </p>
                </div>
                """.formatted(username, appName, appName);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Email thông báo đổi mật khẩu đã gửi đến: " + toEmail);
            
        } catch (MessagingException e) {
            System.err.println("❌ Lỗi gửi email thông báo: " + e.getMessage());
        }
    }
}