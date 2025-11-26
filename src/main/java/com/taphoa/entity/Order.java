package com.taphoa.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false, length = 500)
    private String address;
    
    @Column(length = 1000)
    private String note;
    
    // ✅ THÊM MỚI: Tổng tiền GỐC (trước giảm giá)
    @Column(name = "subtotal_amount")
    private Double subtotalAmount;
    
    // ✅ THÊM MỚI: Số tiền giảm
    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;
    
    // ✅ THÊM MỚI: Mã coupon đã dùng
    @Column(name = "coupon_code")
    private String couponCode;
    
    // Tổng tiền SAU GIẢM (totalAmount = subtotalAmount - discountAmount)
    @Column(nullable = false)
    private Double totalAmount;
    
    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}