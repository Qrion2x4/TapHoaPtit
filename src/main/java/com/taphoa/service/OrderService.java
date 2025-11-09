package com.taphoa.service;

import com.taphoa.entity.*;
import com.taphoa.repository.OrderRepository;
import com.taphoa.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CartService cartService;
    
    @Transactional
    public Order createOrder(User user, String phone, String address, String note) {
        // Lấy giỏ hàng
        List<CartItem> cartItems = cartService.getCartItems(user);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }
        
        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setTotalAmount(cartService.getCartTotal(user));
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
        
        System.out.println("Order created successfully! ID: " + savedOrder.getId());
        
        return savedOrder;
    }
    
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