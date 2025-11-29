package com.taphoa.service;

import com.taphoa.entity.*;
import com.taphoa.repository.OrderRepository;
import com.taphoa.repository.OrderItemRepository;
import com.taphoa.repository.ProductRepository;
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

    @Autowired
    private ProductRepository productRepository;


    @Transactional
    public Order createOrderFromSelectedItems(User user,
                                              List<Long> selectedCartItemIds,
                                              String phone,
                                              String address,
                                              String note,
                                              String couponCode,
                                              Double discountAmount) {
        System.out.println("=== CREATE ORDER FROM SELECTED ITEMS ===");
        System.out.println("User: " + user.getUsername());
        System.out.println("Selected IDs: " + selectedCartItemIds);
        System.out.println("Coupon Code: " + couponCode);
        System.out.println("Discount Amount: " + discountAmount);

        List<CartItem> allCartItems = cartService.getCartItems(user);


        List<CartItem> selectedCartItems = allCartItems.stream()
                .filter(item -> selectedCartItemIds.contains(item.getId()))
                .collect(Collectors.toList());

        System.out.println("Found " + selectedCartItems.size() + " selected items");

        if (selectedCartItems.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sản phẩm đã chọn!");
        }


        double subtotal = selectedCartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();


        double finalDiscount = (discountAmount != null && discountAmount > 0) ? discountAmount : 0.0;


        double totalAmount = subtotal - finalDiscount;

        System.out.println("Subtotal: " + subtotal);
        System.out.println("Discount: " + finalDiscount);
        System.out.println("Total: " + totalAmount);


        Order order = new Order();
        order.setUser(user);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setSubtotalAmount(subtotal);           //  Tổng GỐC
        order.setDiscountAmount(finalDiscount);      //  Số tiền giảm
        order.setCouponCode(couponCode);             //  Mã coupon
        order.setTotalAmount(totalAmount);           //  Tổng SAU GIẢM
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);


        for (CartItem cartItem : selectedCartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);

            System.out.println("  - Added: " + cartItem.getProduct().getName() + " x" + cartItem.getQuantity());
        }


        for (CartItem cartItem : selectedCartItems) {
            cartService.removeFromCart(cartItem.getId());
            System.out.println("  - Removed from cart: " + cartItem.getProduct().getName());
        }

        System.out.println("✅ Order created from selected items with discount! ID: " + savedOrder.getId());

        return savedOrder;
    }
    @Transactional
    public void updateStatusAndHandleStock(Long orderId, String newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        String oldStatus = order.getStatus();


        if (!"COMPLETED".equals(oldStatus) && "COMPLETED".equals(newStatus)) {

            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                int qty = item.getQuantity();

                if (product.getStock() < qty) {
                    throw new RuntimeException(
                            "Sản phẩm '" + product.getName() +
                                    "' không đủ kho (còn " + product.getStock() +
                                    ", cần " + qty + ")"
                    );
                }

                product.setStock(product.getStock() - qty);
                productRepository.save(product);
            }
        }


        order.setStatus(newStatus);
        orderRepository.save(order);
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


    @Transactional
    public void requestCancelOrder(Long orderId, String reason, String note) throws Exception {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            throw new Exception("Không tìm thấy đơn hàng!");
        }

        // 2. Xử lý logic ghép chuỗi lý do 
        String fullReason = "Khách hủy: " + (reason != null ? reason : "Lý do khác");

        // Nếu có ghi chú thêm thì nối vào
        if (note != null && !note.isEmpty()) {
            fullReason += " (" + note + ")";
        }

        // 3. Cập nhật vào Ghi chú đơn hàng 
        String oldNote = order.getNote() != null ? order.getNote() : "";
        String newNote = oldNote.isEmpty() ? fullReason : oldNote + " | " + fullReason;
        order.setNote(newNote);

        // 4. Đổi trạng thái sang Chờ Hủy
        order.setStatus("CANCEL_REQUESTED");

        // 5. Lưu xuống DB
        orderRepository.save(order);

        System.out.println("✅ Đã cập nhật yêu cầu hủy cho đơn #" + orderId + ": " + fullReason);
    }

    @Transactional
    public void approveCancel(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);

        // Chỉ duyệt nếu đơn đang ở trạng thái Yêu cầu hủy
        if (order != null && "CANCEL_REQUESTED".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);

            System.out.println("✅ Admin approved cancel for order #" + orderId);
        }
    }

    @Transactional
    public void rejectCancel(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);


        if (order != null && "CANCEL_REQUESTED".equals(order.getStatus())) {


            order.setStatus("CONFIRMED");


            String oldNote = order.getNote() != null ? order.getNote() : "";
            String rejectMessage = "Shop đã từ chối yêu cầu hủy đơn";


            String newNote = oldNote.isEmpty() ? rejectMessage : oldNote + " | " + rejectMessage;
            order.setNote(newNote);


            orderRepository.save(order);

            System.out.println("❌ Admin rejected cancel for order #" + orderId);
        }
    }

}