package com.taphoa.controller;

import com.taphoa.entity.Product;
import com.taphoa.entity.User;
import com.taphoa.service.CartService;
import com.taphoa.service.ProductService;
import com.taphoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class MainController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;
    
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        
        if (username != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                User user = userService.getUserById(userId);
                if (user != null) {
                    // Nếu là admin thì redirect về trang admin
                    if ("ADMIN".equals(user.getRole())) {
                        return "redirect:/admin";
                    }
                    
                    int cartCount = cartService.getCartCount(user);
                    model.addAttribute("cartCount", cartCount);
                }
            }
        }
        
        List<Product> featuredProducts = productService.getFeaturedProducts();
        List<Product> allProducts = productService.getAllProducts();
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("allProducts", allProducts);
        
        return "index";
    }
    
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        // Nếu đã đăng nhập rồi thì redirect về trang chủ
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        try {
            User user = userService.authenticate(username, password);
            if (user != null) {
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userId", user.getId());
                session.setAttribute("userRole", user.getRole());
                
                // LOG
                System.out.println("=== LOGIN SUCCESS ===");
                System.out.println("Username: " + user.getUsername());
                System.out.println("UserId: " + user.getId());
                System.out.println("Role: " + user.getRole());
                System.out.println("Session ID: " + session.getId());
                
                // Nếu là ADMIN thì redirect về /admin
                if ("ADMIN".equals(user.getRole())) {
                    return "redirect:/admin";
                }
                
                // Nếu là USER thì về trang chủ
                return "redirect:/";
            } else {
                model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
                return "login";
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String email,
                          Model model) {
        try {
            userService.registerUser(username, password, email);
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        System.out.println("=== LOGOUT ===");
        System.out.println("Session ID before invalidate: " + session.getId());
        session.invalidate();
        return "redirect:/";
    }
    
    @GetMapping("/products/category/{category}")
    public String productsByCategory(@PathVariable String category, Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        
        if (username != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                User user = userService.getUserById(userId);
                if (user != null) {
                    int cartCount = cartService.getCartCount(user);
                    model.addAttribute("cartCount", cartCount);
                }
            }
        }
        
        List<Product> products = productService.getProductsByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        
        return "products";
    }
    
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        
        if (username != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                User user = userService.getUserById(userId);
                if (user != null) {
                    int cartCount = cartService.getCartCount(user);
                    model.addAttribute("cartCount", cartCount);
                }
            }
        }
        
        List<Product> products = productService.searchProducts(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        
        return "products";
    }
}