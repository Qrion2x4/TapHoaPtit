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
     * ĐẶT HÀNG CHỈ SẢN PHẨM ĐÃ CHỌN  - CÓ COUPON
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
     *  HỦY ĐƠN HÀNG
     */
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId,
                              @RequestParam(required = false) String reason, // Nhận lý do
                              @RequestParam(required = false) String note,   // Nhận chi tiết
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.getUserById(userId);
            Order order = orderService.getOrderById(orderId);

            // Kiểm tra tồn tại
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Không tìm thấy đơn hàng!");
                return "redirect:/my-orders";
            }

            // Kiểm tra quyền sở hữu
            if (!order.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "❌ Bạn không có quyền hủy đơn hàng này!");
                return "redirect:/my-orders";
            }

            String currentStatus = order.getStatus();

            // --- XỬ LÝ HỦY ĐƠN ---
            if ("PENDING".equals(currentStatus)) {
                // Đơn mới -> Hủy ngay lập tức
                orderService.updateOrderStatus(orderId, "CANCELLED");
                redirectAttributes.addFlashAttribute("success", "✅ Đã hủy đơn hàng thành công!");

            } else if ("CONFIRMED".equals(currentStatus)) {
                // Đơn đã xác nhận -> Gọi Service để lưu lý do và chuyển trạng thái chờ duyệt
                orderService.requestCancelOrder(orderId, reason, note);

                redirectAttributes.addFlashAttribute("warning",
                        "⏳ Đã gửi yêu cầu hủy đơn hàng. Vui lòng đợi cửa hàng xác nhận!");

            } else if ("SHIPPING".equals(currentStatus)) {
                redirectAttributes.addFlashAttribute("error", "❌ Đơn hàng đang giao, không thể hủy!");

            } else if ("COMPLETED".equals(currentStatus)) {
                redirectAttributes.addFlashAttribute("error", "❌ Đơn hàng đã hoàn thành, không thể hủy!");

            } else {
                redirectAttributes.addFlashAttribute("info", "ℹ️ Đơn hàng đã hủy hoặc đang chờ xử lý.");
            }

            return "redirect:/my-orders";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/my-orders";
        }
    }
}