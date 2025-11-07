package com.taphoa.controller;

import com.taphoa.entity.User;
import com.taphoa.entity.CartItem;
import com.taphoa.service.CartService;
import com.taphoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
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
            List<CartItem> cartItems = cartService.getCartItems(user);
            
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
                return "redirect:/cart";
            }
            
            // LOG ORDER INFO
            System.out.println("=== NEW ORDER ===");
            System.out.println("User: " + user.getUsername());
            System.out.println("Phone: " + phone);
            System.out.println("Address: " + address);
            System.out.println("Note: " + note);
            System.out.println("Total items: " + cartItems.size());
            
            double total = cartService.getCartTotal(user);
            System.out.println("Total: " + total);
            
            // TODO: Save order to database here
            // For now, just clear cart
            cartService.clearCart(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt hàng thành công! Chúng tôi sẽ liên hệ với bạn sớm nhất.");
            
            return "redirect:/";
            
        } catch (Exception e) {
            System.out.println("ERROR placing order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đặt hàng!");
            return "redirect:/cart";
        }
    }
}