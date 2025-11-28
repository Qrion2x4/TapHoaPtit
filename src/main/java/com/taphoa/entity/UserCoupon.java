package com.taphoa.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
@Data
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }
}