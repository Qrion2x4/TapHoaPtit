package com.taphoa.service;

import com.taphoa.entity.User;
import com.taphoa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Đăng ký user mới với xác thực email
     */
    public User registerUser(String username, String password, String email, String fullName, String phone) {
        // Kiểm tra username
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        
        // Kiểm tra email
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        
        // Tạo user mới
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        
        // Tạo verification token
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24)); // Hết hạn sau 24h
        user.setEmailVerified(false);
        
        // Lưu user
        User savedUser = userRepository.save(user);
        
        // Gửi email xác thực
        try {
            emailService.sendVerificationEmail(email, username, token);
            System.out.println("✅ Đã gửi email xác thực đến: " + email);
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email: " + e.getMessage());
            // Vẫn cho phép đăng ký thành công dù email fail
        }
        
        return savedUser;
    }
    
    /**
     * Xác thực email bằng token
     */
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Kiểm tra token còn hạn không
        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return false; // Token hết hạn
        }
        
        // Xác thực thành công
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        
        return true;
    }
    
    /**
     * Đăng nhập - CHỈ cho phép nếu đã xác thực email (TRỪ ADMIN)
     * ✅ ADMIN không cần xác thực email
     */
    public User authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        
        User user = userOpt.get();
        
        // Kiểm tra password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        
        // ⚠️ QUAN TRỌNG: Kiểm tra đã xác thực email chưa
        // ✅ ADMIN được bỏ qua yêu cầu xác thực email
        if (!user.isEmailVerified() && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Tài khoản chưa được xác thực! Vui lòng kiểm tra email để xác thực tài khoản.");
        }
        
        return user;
    }
    
    /**
     * Yêu cầu reset mật khẩu - gửi email với token
     */
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tài khoản với email này!");
        }
        
        User user = userOpt.get();
        
        // Tạo reset token
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1)); // Hết hạn sau 1h
        
        userRepository.save(user);
        
        // Gửi email
        emailService.sendResetPasswordEmail(email, user.getUsername(), token);
    }
    
    /**
     * Xác thực reset password token
     */
    public User validateResetToken(String token) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(token);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Token không hợp lệ!");
        }
        
        User user = userOpt.get();
        
        // Kiểm tra token còn hạn không
        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn! Vui lòng yêu cầu reset mật khẩu lại.");
        }
        
        return user;
    }
    
    /**
     * Reset mật khẩu với token
     */
    public void resetPassword(String token, String newPassword) {
        User user = validateResetToken(token);
        
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        
        userRepository.save(user);
        
        // Gửi email thông báo
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
    }
    
    /**
     * Đổi mật khẩu (khi đã đăng nhập)
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
        
        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng!");
        }
        
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Gửi email thông báo
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
    }
    
    /**
     * Gửi lại email xác thực
     */
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy email này!");
        }
        
        User user = userOpt.get();
        
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email đã được xác thực rồi!");
        }
        
        // Tạo token mới
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        
        userRepository.save(user);
        
        // Gửi email
        emailService.sendVerificationEmail(email, user.getUsername(), token);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}