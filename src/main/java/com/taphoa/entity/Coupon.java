package com.taphoa.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code; // VD: NEWBIE, SUMMER2025
    
    @Column(nullable = false)
    private String name; // Tên mã giảm giá
    
    @Column(nullable = false)
    private Double discountPercent; // % giảm giá (10 = 10%)
    
    private Double maxDiscount; // Giảm tối đa (VD: 50000đ)
    
    private Double minOrderAmount; // Đơn tối thiểu (VD: 100000đ)
    
    @Column(nullable = false)
    private Boolean active = true; // Còn hiệu lực không
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}