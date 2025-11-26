package com.taphoa.service;

import com.taphoa.entity.*;
import com.taphoa.repository.OrderRepository;
import com.taphoa.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CartService cartService;
    
    /**
     * TẠO ĐƠN HÀNG VỚI TẤT CẢ SẢN PHẨM (API CŨ - vẫn giữ)
     */
    @Transactional
    public Order createOrder(User user, String phone, String address, String note) {
        System.out.println("=== CREATE ORDER (ALL ITEMS) ===");
        
        // Lấy giỏ hàng
        List<CartItem> cartItems = cartService.getCartItems(user);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }
        
        double totalAmount = cartService.getCartTotal(user);
        
        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setSubtotalAmount(totalAmount);
        order.setDiscountAmount(0.0);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        
        Order savedOrder = orderRepository.save(order);
        
        // Tạo order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);
        }
        
        // Xóa giỏ hàng
        cartService.clearCart(user);
        
        System.out.println("✅ Order created! ID: " + savedOrder.getId());
        
        return savedOrder;
    }
    
    /**
     * ✅ TẠO ĐƠN HÀNG CHỈ VỚI CÁC SẢN PHẨM ĐÃ CHỌN + COUPON (API MỚI)
     */
    @Transactional
    public Order createOrderFromSelectedItems(User user, 
                                             List<Long> selectedCartItemIds, 
                                             String phone, 
                                             String address, 
                                             String note,
                                             String couponCode,      // ✅ THÊM
                                             Double discountAmount) { // ✅ THÊM
        System.out.println("=== CREATE ORDER FROM SELECTED ITEMS ===");
        System.out.println("User: " + user.getUsername());
        System.out.println("Selected IDs: " + selectedCartItemIds);
        System.out.println("Coupon Code: " + couponCode);
        System.out.println("Discount Amount: " + discountAmount);
        
        // Lấy TẤT CẢ cart items của user
        List<CartItem> allCartItems = cartService.getCartItems(user);
        
        // Lọc chỉ lấy các item đã được chọn
        List<CartItem> selectedCartItems = allCartItems.stream()
                .filter(item -> selectedCartItemIds.contains(item.getId()))
                .collect(Collectors.toList());
        
        System.out.println("Found " + selectedCartItems.size() + " selected items");
        
        if (selectedCartItems.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sản phẩm đã chọn!");
        }
        
        // ✅ TÍNH TỔNG TIỀN GỐC (subtotal) - TRƯỚC GIẢM GIÁ
        double subtotal = selectedCartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        
        // ✅ XỬ LÝ DISCOUNT
        double finalDiscount = (discountAmount != null && discountAmount > 0) ? discountAmount : 0.0;
        
        // ✅ TÍNH TỔNG TIỀN SAU GIẢM (totalAmount)
        double totalAmount = subtotal - finalDiscount;
        
        System.out.println("Subtotal: " + subtotal);
        System.out.println("Discount: " + finalDiscount);
        System.out.println("Total: " + totalAmount);
        
        // ✅ TẠO ĐƠN HÀNG VỚI ĐẦY ĐỦ THÔNG TIN
        Order order = new Order();
        order.setUser(user);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setSubtotalAmount(subtotal);           // ✅ Tổng GỐC
        order.setDiscountAmount(finalDiscount);      // ✅ Số tiền giảm
        order.setCouponCode(couponCode);             // ✅ Mã coupon
        order.setTotalAmount(totalAmount);           // ✅ Tổng SAU GIẢM
        order.setStatus("PENDING");
        
        Order savedOrder = orderRepository.save(order);
        
        // Tạo order items CHỈ từ các sản phẩm đã chọn
        for (CartItem cartItem : selectedCartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);
            
            System.out.println("  - Added: " + cartItem.getProduct().getName() + " x" + cartItem.getQuantity());
        }
        
        // XÓA CHỈ CÁC SẢN PHẨM ĐÃ CHỌN khỏi giỏ hàng
        for (CartItem cartItem : selectedCartItems) {
            cartService.removeFromCart(cartItem.getId());
            System.out.println("  - Removed from cart: " + cartItem.getProduct().getName());
        }
        
        System.out.println("✅ Order created from selected items with discount! ID: " + savedOrder.getId());
        
        return savedOrder;
    }
    
    /**
     * CÁC METHOD CŨ - GIỮ NGUYÊN
     */
    
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);
            System.out.println("Order " + orderId + " status updated to: " + status);
        }
    }
    
    public long countOrdersByStatus(String status) {
        return orderRepository.findByStatus(status).size();
    }
}