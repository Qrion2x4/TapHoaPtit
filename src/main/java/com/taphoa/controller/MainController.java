package com.taphoa.controller;

import com.taphoa.entity.Order;
import com.taphoa.entity.Product;
import com.taphoa.entity.User;
import com.taphoa.service.CartService;
import com.taphoa.service.OrderService;
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
    
    @Autowired
    private OrderService orderService;
    
    // ============================================
    // TRANG CHỦ
    // ============================================
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        
        if (username != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                User user = userService.getUserById(userId);
                if (user != null) {
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
    
    // ============================================
    // ĐĂNG NHẬP
    // ============================================
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
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
            
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole());
            
            System.out.println("=== LOGIN SUCCESS ===");
            System.out.println("Username: " + user.getUsername());
            System.out.println("UserId: " + user.getId());
            System.out.println("Role: " + user.getRole());
            
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin";
            }
            
            return "redirect:/";
            
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
    
    // ============================================
    // ĐĂNG KÝ
    // ============================================
    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam String email,
                          @RequestParam String fullName,
                          @RequestParam(required = false) String phone,
                          Model model) {
        try {
            // Kiểm tra mật khẩu khớp
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "register";
            }
            
            // Đăng ký user
            userService.registerUser(username, password, email, fullName, phone);
            
            model.addAttribute("success", "✅ Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            return "login";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    // ============================================
    // XÁC THỰC EMAIL
    // ============================================
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            boolean verified = userService.verifyEmail(token);
            
            if (verified) {
                model.addAttribute("success", "🎉 Email đã được xác thực thành công! Bạn có thể đăng nhập ngay bây giờ.");
            } else {
                model.addAttribute("error", "❌ Link xác thực không hợp lệ hoặc đã hết hạn!");
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "❌ Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "verify-email";
    }
    
    // ============================================
    // QUÊN MẬT KHẨU
    // ============================================
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
            userService.requestPasswordReset(email);
            model.addAttribute("success", "✅ Đã gửi link đặt lại mật khẩu đến email của bạn. Vui lòng kiểm tra hộp thư!");
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        
        return "forgot-password";
    }
    
    // ============================================
    // RESET MẬT KHẨU
    // ============================================
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        try {
            // Kiểm tra token có hợp lệ không
            userService.validateResetToken(token);
            model.addAttribute("token", token);
            return "reset-password";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {
        try {
            // Kiểm tra mật khẩu khớp
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
                model.addAttribute("token", token);
                return "reset-password";
            }
            
            // Reset password
            userService.resetPassword(token, password);
            model.addAttribute("success", "✅ Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.");
            return "login";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
    
    // ============================================
    // ĐỔI MẬT KHẨU (KHI ĐÃ ĐĂNG NHẬP)
    // ============================================
    @GetMapping("/change-password")
    public String changePasswordPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("username", session.getAttribute("username"));
        return "change-password";
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            // Kiểm tra mật khẩu mới khớp
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "change-password";
            }
            
            // Đổi mật khẩu
            userService.changePassword(userId, oldPassword, newPassword);
            model.addAttribute("success", "✅ Đổi mật khẩu thành công!");
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        
        model.addAttribute("username", session.getAttribute("username"));
        return "change-password";
    }
    
    // ============================================
    // ĐĂNG XUẤT
    // ============================================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        System.out.println("=== LOGOUT ===");
        session.invalidate();
        return "redirect:/";
    }
    
    // ============================================
    // SẢN PHẨM THEO DANH MỤC
    // ============================================
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
        
        return "category";
    }
    
    // ============================================
    // TÌM KIẾM
    // ============================================
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
        model.addAttribute("category", "Kết quả tìm kiếm: " + keyword);
        
        return "category";
    }
    
    // ============================================
    // ĐỌN HÀNG CỦA TÔI
    // ============================================
    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(userId);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Order> orders = orderService.getUserOrders(user);
        
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("orders", orders);
        model.addAttribute("cartCount", cartService.getCartCount(user));
        
        return "my-orders";
    }
}