package com.taphoa.controller;

import com.taphoa.entity.User;
import com.taphoa.entity.Order;
import com.taphoa.entity.Coupon;
import com.taphoa.service.OrderService;
import com.taphoa.service.UserService;
import com.taphoa.service.CouponService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CouponService couponService;
    
    /**
     * ĐẶT HÀNG TẤT CẢ SẢN PHẨM (API CŨ - vẫn giữ)
     */
    @PostMapping("/place")
    public String placeOrder(@RequestParam String phone,
                            @RequestParam String address,
                            @RequestParam(required = false) String note,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            User user = userService.getUserById(userId);
            Order order = orderService.createOrder(user, phone, address, note);
            
            redirectAttributes.addFlashAttribute("success", 
                "✅ Đặt hàng thành công! Mã đơn hàng: #" + order.getId());
            
            return "redirect:/my-orders";
            
        } catch (Exception e) {
            System.out.println("ERROR placing order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }
    
    /**
     * ĐẶT HÀNG CHỈ SẢN PHẨM ĐÃ CHỌN (API MỚI) - CÓ COUPON
     */
    @PostMapping("/place-selected")
    public String placeSelectedOrder(@RequestParam String phone,
                                    @RequestParam String address,
                                    @RequestParam(required = false) String note,
                                    @RequestParam String selectedItemIds,
                                    @RequestParam(required = false) String couponCode,
                                    @RequestParam(required = false, defaultValue = "0") Double discountAmount,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        
        System.out.println("=== PLACE SELECTED ORDER ===");
        System.out.println("User ID: " + userId);
        System.out.println("Selected Item IDs: " + selectedItemIds);
        System.out.println("Phone: " + phone);
        System.out.println("Address: " + address);
        System.out.println("Coupon Code: " + couponCode);
        System.out.println("Discount Amount: " + discountAmount);
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            User user = userService.getUserById(userId);
            
            List<Long> cartItemIds = Arrays.stream(selectedItemIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            System.out.println("Parsed IDs: " + cartItemIds);
            
            if (cartItemIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất 1 sản phẩm!");
                return "redirect:/cart";
            }
            
            Order order = orderService.createOrderFromSelectedItems(
                user, 
                cartItemIds, 
                phone, 
                address, 
                note,
                couponCode,
                discountAmount
            );
            
            if (couponCode != null && !couponCode.isEmpty() && discountAmount > 0) {
                Coupon coupon = couponService.getCouponByCode(couponCode);
                if (coupon != null) {
                    couponService.markCouponAsUsed(user, coupon, order.getId());
                    System.out.println("✅ Marked coupon as used: " + couponCode);
                }
            }
            
            System.out.println("✅ Order created successfully! ID: " + order.getId());
            
            String successMessage = "✅ Đặt hàng thành công! Mã đơn hàng: #" + order.getId() + 
                " (" + cartItemIds.size() + " sản phẩm)";
            
            if (discountAmount > 0) {
                successMessage += " - Đã giảm " + String.format("%,.0f", discountAmount) + "₫";
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage);
            
            return "redirect:/my-orders";
            
        } catch (Exception e) {
            System.out.println("❌ ERROR placing selected order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }
    
    /**
     * ✅ HỦY ĐƠN HÀNG - CUSTOMER
     */
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        
        System.out.println("=== CANCEL ORDER REQUEST ===");
        System.out.println("Order ID: " + orderId);
        System.out.println("User ID: " + userId);
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            User user = userService.getUserById(userId);
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra đơn hàng có tồn tại không
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Không tìm thấy đơn hàng!");
                return "redirect:/my-orders";
            }
            
            // Kiểm tra quyền sở hữu đơn hàng
            if (!order.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "❌ Bạn không có quyền hủy đơn hàng này!");
                return "redirect:/my-orders";
            }
            
            String currentStatus = order.getStatus();
            System.out.println("Current status: " + currentStatus);
            
            // ✅ LOGIC HỦY ĐƠN
            if ("PENDING".equals(currentStatus)) {
                // Đơn chưa xác nhận → Hủy trực tiếp
                orderService.updateOrderStatus(orderId, "CANCELLED");
                redirectAttributes.addFlashAttribute("success", 
                    "✅ Đã hủy đơn hàng #" + orderId + " thành công!");
                System.out.println("✅ Order cancelled directly (PENDING)");
                
            } else if ("CONFIRMED".equals(currentStatus)) {
                // Đơn đã xác nhận → Đổi sang CANCEL_REQUESTED (chờ admin duyệt)
                orderService.updateOrderStatus(orderId, "CANCEL_REQUESTED");
                redirectAttributes.addFlashAttribute("warning", 
                    "⏳ Đã gửi yêu cầu hủy đơn hàng #" + orderId + ". Vui lòng đợi xác nhận từ cửa hàng!");
                System.out.println("⏳ Cancel request sent (CONFIRMED)");
                
            } else if ("SHIPPING".equals(currentStatus)) {
                // Đơn đang giao → Không cho hủy
                redirectAttributes.addFlashAttribute("error", 
                    "❌ Không thể hủy đơn hàng #" + orderId + " vì đơn hàng đang được giao!");
                System.out.println("❌ Cannot cancel (SHIPPING)");
                
            } else if ("COMPLETED".equals(currentStatus)) {
                // Đơn đã giao thành công → Không cho hủy
                redirectAttributes.addFlashAttribute("error", 
                    "❌ Không thể hủy đơn hàng #" + orderId + " vì đơn hàng đã hoàn thành!");
                System.out.println("❌ Cannot cancel (COMPLETED)");
                
            } else if ("CANCELLED".equals(currentStatus) || "CANCEL_REQUESTED".equals(currentStatus)) {
                // Đơn đã hủy hoặc đang chờ hủy
                redirectAttributes.addFlashAttribute("info", 
                    "ℹ️ Đơn hàng #" + orderId + " đã được hủy hoặc đang chờ xử lý!");
                System.out.println("ℹ️ Already cancelled or pending");
                
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "❌ Không thể hủy đơn hàng #" + orderId + " ở trạng thái hiện tại!");
            }
            
            return "redirect:/my-orders";
            
        } catch (Exception e) {
            System.out.println("❌ ERROR cancelling order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/my-orders";
        }
    }
}