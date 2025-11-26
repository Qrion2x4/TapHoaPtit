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
            
            // Tạo đơn hàng với TẤT CẢ sản phẩm trong giỏ
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
     * ✅ ĐẶT HÀNG CHỈ SẢN PHẨM ĐÃ CHỌN (API MỚI) - CÓ COUPON
     */
    @PostMapping("/place-selected")
    public String placeSelectedOrder(@RequestParam String phone,
                                    @RequestParam String address,
                                    @RequestParam(required = false) String note,
                                    @RequestParam String selectedItemIds,
                                    @RequestParam(required = false) String couponCode,  // ✅ THÊM
                                    @RequestParam(required = false, defaultValue = "0") Double discountAmount, // ✅ THÊM
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
            
            // Parse danh sách ID đã chọn
            List<Long> cartItemIds = Arrays.stream(selectedItemIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            System.out.println("Parsed IDs: " + cartItemIds);
            
            if (cartItemIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất 1 sản phẩm!");
                return "redirect:/cart";
            }
            
            // ✅ Tạo đơn hàng CHỈ với các sản phẩm đã chọn + COUPON
            Order order = orderService.createOrderFromSelectedItems(
                user, 
                cartItemIds, 
                phone, 
                address, 
                note,
                couponCode,      // ✅ TRUYỀN COUPON
                discountAmount   // ✅ TRUYỀN DISCOUNT
            );
            
            // ✅ ĐÁNH DẤU COUPON ĐÃ DÙNG
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
}