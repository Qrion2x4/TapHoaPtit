package com.taphoa.service;

import com.taphoa.entity.Coupon;
import com.taphoa.entity.User;
import com.taphoa.entity.UserCoupon;
import com.taphoa.repository.CouponRepository;
import com.taphoa.repository.UserCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CouponService {
    
    @Autowired
    private CouponRepository couponRepository;
    
    @Autowired
    private UserCouponRepository userCouponRepository;
    
    /**
     * Kiểm tra mã giảm giá
     */
    public Map<String, Object> validateCoupon(String code, User user, Double orderTotal) {
        Map<String, Object> result = new HashMap<>();
        
        // Tìm coupon
        Optional<Coupon> couponOpt = couponRepository.findByCodeAndActiveTrue(code);
        if (couponOpt.isEmpty()) {
            result.put("valid", false);
            result.put("message", "Mã giảm giá không tồn tại hoặc đã hết hạn!");
            return result;
        }
        
        Coupon coupon = couponOpt.get();
        
        // Kiểm tra thời gian
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            result.put("valid", false);
            result.put("message", "Mã giảm giá chưa có hiệu lực!");
            return result;
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            result.put("valid", false);
            result.put("message", "Mã giảm giá đã hết hạn!");
            return result;
        }
        
        // Kiểm tra đơn tối thiểu
        if (coupon.getMinOrderAmount() != null && orderTotal < coupon.getMinOrderAmount()) {
            result.put("valid", false);
            result.put("message", "Đơn hàng tối thiểu " + String.format("%,.0f", coupon.getMinOrderAmount()) + "₫!");
            return result;
        }
        
        // Kiểm tra đã dùng chưa
        if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
            result.put("valid", false);
            result.put("message", "Bạn đã sử dụng mã này rồi!");
            return result;
        }
        
        // Tính giảm giá
        Double discount = orderTotal * (coupon.getDiscountPercent() / 100);
        if (coupon.getMaxDiscount() != null && discount > coupon.getMaxDiscount()) {
            discount = coupon.getMaxDiscount();
        }
        
        result.put("valid", true);
        result.put("coupon", coupon);
        result.put("discount", discount);
        result.put("finalAmount", orderTotal - discount);
        result.put("message", "Áp dụng mã thành công! Giảm " + String.format("%,.0f", discount) + "₫");
        
        return result;
    }
    
    /**
     * Lưu lại mã đã dùng
     */
    @Transactional
    public void markCouponAsUsed(User user, Coupon coupon, Long orderId) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setCoupon(coupon);
        userCoupon.setOrderId(orderId);
        userCouponRepository.save(userCoupon);
        System.out.println("✅ Marked coupon " + coupon.getCode() + " as used for order #" + orderId);
    }
    
    /**
     * Lấy coupon theo code
     */
    public Coupon getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        Optional<Coupon> couponOpt = couponRepository.findByCodeAndActiveTrue(code.toUpperCase().trim());
        return couponOpt.orElse(null);
    }
    
    /**
     * Tạo mã NEWBIE
     */
    @Transactional
    public void createNewbieCoupon() {
        // Kiểm tra đã tồn tại chưa
        if (couponRepository.findByCodeAndActiveTrue("NEWBIE").isPresent()) {
            return;
        }
        
        Coupon coupon = new Coupon();
        coupon.setCode("NEWBIE");
        coupon.setName("Mã giảm giá cho người dùng mới");
        coupon.setDiscountPercent(10.0); // Giảm 10%
        coupon.setMaxDiscount(50000.0);  // Tối đa 50k
        coupon.setMinOrderAmount(100000.0); // Đơn tối thiểu 100k
        coupon.setActive(true);
        coupon.setValidFrom(LocalDateTime.now());
        coupon.setValidUntil(LocalDateTime.now().plusYears(10)); // Vĩnh viễn
        
        couponRepository.save(coupon);
        System.out.println("✅ Created NEWBIE coupon: 10% off, max 50k");
    }
}