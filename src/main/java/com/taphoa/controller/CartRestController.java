package com.taphoa.controller;

import com.taphoa.entity.CartItem;
import com.taphoa.entity.User;
import com.taphoa.service.CartService;
import com.taphoa.service.UserService;
import com.taphoa.service.CouponService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/cart")
public class CartRestController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CouponService couponService;
    

    @PostMapping("/add")
    public ResponseEntity<?> addToCartAjax(@RequestParam Long productId,
                                           @RequestParam(defaultValue = "1") Integer quantity,
                                           HttpSession session) {
        try {
            System.out.println("=== API: ADD TO CART (AJAX) ===");
            System.out.println("Product ID: " + productId);
            System.out.println("Quantity: " + quantity);
            
            // Kiểm tra đăng nhập
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập!"));
            }
            
            // Lấy user
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Không tìm thấy người dùng!"));
            }
            
            System.out.println("User: " + user.getUsername());
            

            cartService.addToCart(user, productId, quantity);
            
            System.out.println("✅ Added to cart successfully!");
            

            int newCartCount = cartService.getCartCount(user);
            Double newCartTotal = cartService.getCartTotal(user);
            
            System.out.println("New cart count: " + newCartCount);
            
            // Trả về JSON
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartCount", newCartCount);
            response.put("cartTotal", newCartTotal);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    

    @PostMapping("/increase/{id}")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long id, HttpSession session) {
        try {
            System.out.println("=== API: INCREASE QUANTITY ===");
            System.out.println("Cart Item ID: " + id);
            
            // Kiểm tra đăng nhập
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập!"));
            }
            
            // Lấy cart item
            CartItem item = cartService.getCartItemById(id);
            if (item == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Không tìm thấy sản phẩm!"));
            }
            
            // Kiểm tra user có quyền không
            if (!item.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("Không có quyền!"));
            }
            
            // Tăng số lượng
            int newQuantity = item.getQuantity() + 1;
            cartService.updateQuantity(id, newQuantity);
            
            System.out.println("✅ Increased: " + item.getQuantity() + " → " + newQuantity);
            
            // Lấy thông tin mới
            User user = userService.getUserById(userId);
            Double newTotal = cartService.getCartTotal(user);
            int newCartCount = cartService.getCartCount(user);
            
            // Trả về JSON
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newQuantity", newQuantity);
            response.put("itemTotal", item.getProduct().getPrice() * newQuantity);
            response.put("cartTotal", newTotal);
            response.put("cartCount", newCartCount);
            response.put("message", "Đã tăng số lượng!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    

    @PostMapping("/decrease/{id}")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long id, HttpSession session) {
        try {
            System.out.println("=== API: DECREASE QUANTITY ===");
            System.out.println("Cart Item ID: " + id);
            

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập!"));
            }
            

            CartItem item = cartService.getCartItemById(id);
            if (item == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Không tìm thấy sản phẩm!"));
            }
            

            if (!item.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("Không có quyền!"));
            }
            
            // NẾU quantity = 1, KHÔNG CHO GIẢM
            if (item.getQuantity() <= 1) {
                System.out.println("❌ Quantity is 1, cannot decrease. Use remove instead!");
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Số lượng tối thiểu là 1. Vui lòng dùng nút Xóa để xóa sản phẩm!")
                );
            }
            

            int newQuantity = item.getQuantity() - 1;
            cartService.updateQuantity(id, newQuantity);
            
            System.out.println("✅ Decreased: " + item.getQuantity() + " → " + newQuantity);
            

            User user = userService.getUserById(userId);
            Double newTotal = cartService.getCartTotal(user);
            int newCartCount = cartService.getCartCount(user);
            
            // Trả về JSON
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newQuantity", newQuantity);
            response.put("itemTotal", item.getProduct().getPrice() * newQuantity);
            response.put("cartTotal", newTotal);
            response.put("cartCount", newCartCount);
            response.put("message", "Đã giảm số lượng!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeItem(@PathVariable Long id, HttpSession session) {
        try {
            System.out.println("=== API: REMOVE ITEM ===");
            System.out.println("Cart Item ID: " + id);
            

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập!"));
            }
            

            CartItem item = cartService.getCartItemById(id);
            if (item == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Không tìm thấy sản phẩm!"));
            }
            

            if (!item.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("Không có quyền!"));
            }
            
            // Xóa
            cartService.removeFromCart(id);
            
            System.out.println("✅ Removed successfully");
            

            User user = userService.getUserById(userId);
            Double newTotal = cartService.getCartTotal(user);
            int newCartCount = cartService.getCartCount(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartTotal", newTotal);
            response.put("cartCount", newCartCount);
            response.put("message", "Đã xóa sản phẩm!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    

    @PostMapping("/apply-coupon")
    public ResponseEntity<?> applyCoupon(@RequestParam String code,
                                        @RequestParam Double orderTotal,
                                        HttpSession session) {
        try {
            System.out.println("=== API: APPLY COUPON ===");
            System.out.println("Code: " + code);
            System.out.println("Order Total (Selected Items): " + orderTotal);
            
            // Kiểm tra đăng nhập
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Vui lòng đăng nhập!"));
            }
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Không tìm thấy người dùng!"));
            }
            
            // Validate coupon với orderTotal từ frontend (chỉ tính sản phẩm đã chọn)
            Map<String, Object> result = couponService.validateCoupon(code.toUpperCase().trim(), user, orderTotal);
            
            if ((Boolean) result.get("valid")) {
                // Lưu vào session để dùng khi đặt hàng
                session.setAttribute("appliedCoupon", result.get("coupon"));
                session.setAttribute("discountAmount", result.get("discount"));
                
                System.out.println("✅ Coupon applied successfully! Discount: " + result.get("discount"));
                
                return ResponseEntity.ok(result);
            } else {
                System.out.println("❌ Coupon validation failed: " + result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}