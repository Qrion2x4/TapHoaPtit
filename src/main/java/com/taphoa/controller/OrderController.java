package com.taphoa.controller;

import com.taphoa.entity.User;
import com.taphoa.entity.Order;
import com.taphoa.service.OrderService;
import com.taphoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
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
            
            // Tạo đơn hàng
            Order order = orderService.createOrder(user, phone, address, note);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt hàng thành công! Mã đơn hàng: #" + order.getId());
            
            return "redirect:/";
            
        } catch (Exception e) {
            System.out.println("ERROR placing order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }
}