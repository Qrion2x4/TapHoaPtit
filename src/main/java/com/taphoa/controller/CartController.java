package com.taphoa.controller;

import com.taphoa.entity.CartItem;
import com.taphoa.entity.User;
import com.taphoa.service.CartService;
import com.taphoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        
        System.out.println("=== VIEW CART ===");
        System.out.println("Session ID: " + session.getId());
        System.out.println("User ID: " + userId);
        
        if (userId == null) {
            System.out.println("User not logged in, redirecting to login");
            return "redirect:/login";
        }
        
        User user = userService.getUserById(userId);
        if (user == null) {
            System.out.println("User not found in database!");
            return "redirect:/login";
        }
        
        System.out.println("User: " + user.getUsername());
        
        List<CartItem> cartItems = cartService.getCartItems(user);
        System.out.println("Cart items count: " + cartItems.size());
        
        Double total = cartService.getCartTotal(user);
        int cartCount = cartService.getCartCount(user);
        
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartCount);
        
        return "cart";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           @RequestParam(required = false) String returnUrl,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        
        System.out.println("=== ADD TO CART ===");
        System.out.println("User ID: " + userId);
        System.out.println("Product ID: " + productId);
        System.out.println("Quantity: " + quantity);
        
        if (userId == null) {
            System.out.println("ERROR: User not logged in!");
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm vào giỏ hàng!");
            return "redirect:/login";
        }
        
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                System.out.println("ERROR: User not found!");
                return "redirect:/login";
            }
            
            System.out.println("User found: " + user.getUsername());
            
            cartService.addToCart(user, productId, quantity);
            System.out.println("Added to cart successfully!");
            
            int newCartCount = cartService.getCartCount(user);
            System.out.println("New cart count: " + newCartCount);
            
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng!");
            
        } catch (Exception e) {
            System.out.println("ERROR adding to cart: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        if (returnUrl != null && !returnUrl.isEmpty()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/";
    }
    
    /**
     * TĂNG SỐ LƯỢNG
     */
    @PostMapping("/increase/{id}")
    public String increaseQuantity(@PathVariable Long id, 
                                   RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== INCREASE QUANTITY ===");
            System.out.println("Cart Item ID: " + id);
            
            CartItem item = cartService.getCartItemById(id);
            if (item == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
                return "redirect:/cart";
            }
            
            int newQuantity = item.getQuantity() + 1;
            cartService.updateQuantity(id, newQuantity);
            
            System.out.println("✅ Increased: " + item.getQuantity() + " → " + newQuantity);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra!");
        }
        
        return "redirect:/cart";
    }
    
    /**
     * GIẢM SỐ LƯỢNG
     */
    @PostMapping("/decrease/{id}")
    public String decreaseQuantity(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DECREASE QUANTITY ===");
            System.out.println("Cart Item ID: " + id);
            
            CartItem item = cartService.getCartItemById(id);
            if (item == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
                return "redirect:/cart";
            }
            
            // Nếu quantity = 1, xóa luôn
            if (item.getQuantity() <= 1) {
                cartService.removeFromCart(id);
                System.out.println("✅ Removed item (quantity was 1)");
                redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ!");
            } else {
                int newQuantity = item.getQuantity() - 1;
                cartService.updateQuantity(id, newQuantity);
                System.out.println("✅ Decreased: " + item.getQuantity() + " → " + newQuantity);
            }
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra!");
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, 
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== REMOVE FROM CART ===");
            System.out.println("Cart Item ID: " + id);
            
            cartService.removeFromCart(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            System.out.println("ERROR removing from cart: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra!");
        }
        return "redirect:/cart";
    }
    
    @PostMapping("/update/{id}")
    public String updateQuantity(@PathVariable Long id, 
                                 @RequestParam Integer quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== UPDATE CART ===");
            System.out.println("Cart Item ID: " + id);
            System.out.println("New Quantity: " + quantity);
            
            if (quantity < 1) {
                redirectAttributes.addFlashAttribute("error", "Số lượng không hợp lệ!");
                return "redirect:/cart";
            }
            
            cartService.updateQuantity(id, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật số lượng!");
        } catch (Exception e) {
            System.out.println("ERROR updating cart: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra!");
        }
        return "redirect:/cart";
    }
}