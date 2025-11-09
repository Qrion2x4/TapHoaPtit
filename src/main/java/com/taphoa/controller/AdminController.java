package com.taphoa.controller;

import com.taphoa.entity.Order;
import com.taphoa.entity.Product;
import com.taphoa.entity.User;
import com.taphoa.service.OrderService;
import com.taphoa.service.ProductService;
import com.taphoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    // Middleware kiểm tra admin
    private boolean isAdmin(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return false;
        
        User user = userService.getUserById(userId);
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        // Statistics
        long totalOrders = orderService.getAllOrders().size();
        long pendingOrders = orderService.countOrdersByStatus("PENDING");
        long completedOrders = orderService.countOrdersByStatus("COMPLETED");
        long totalProducts = productService.getAllProducts().size();
        
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("username", session.getAttribute("username"));
        
        return "admin/dashboard";
    }
    
    // ============ QUẢN LÝ ĐƠN HÀNG ============
    
    @GetMapping("/orders")
    public String manageOrders(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("username", session.getAttribute("username"));
        
        return "admin/orders";
    }
    
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra!");
        }
        
        return "redirect:/admin/orders";
    }
    
    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("username", session.getAttribute("username"));
        
        return "admin/order-detail";
    }
    
    // ============ QUẢN LÝ SẢN PHẨM ============
    
    @GetMapping("/products")
    public String manageProducts(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("username", session.getAttribute("username"));
        
        return "admin/products";
    }
    
    @GetMapping("/products/add")
    public String addProductForm(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        model.addAttribute("product", new Product());
        model.addAttribute("username", session.getAttribute("username"));
        
        return "admin/product-form";
    }
    
    @PostMapping("/products/add")
    public String addProduct(@RequestParam String name,
                            @RequestParam String category,
                            @RequestParam Double price,
                            @RequestParam(required = false) Double oldPrice,
                            @RequestParam String imageUrl,
                            @RequestParam Integer stock,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false, defaultValue = "false") Boolean featured,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        System.out.println("=== ADD PRODUCT ===");
        
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setOldPrice(oldPrice);
            product.setImageUrl(imageUrl);
            product.setStock(stock);
            product.setDescription(description);
            product.setFeatured(featured);
            
            productService.saveProduct(product);
            System.out.println("Product added successfully!");
            
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, HttpSession session, Model model) {
        System.out.println("========================================");
        System.out.println("=== EDIT PRODUCT FORM - GET REQUEST ===");
        System.out.println("========================================");
        System.out.println("Product ID: " + id);
        System.out.println("Session ID: " + session.getId());
        System.out.println("User ID: " + session.getAttribute("userId"));
        System.out.println("Username: " + session.getAttribute("username"));
        System.out.println("User Role: " + session.getAttribute("userRole"));
        
        if (!isAdmin(session)) {
            System.out.println("❌ NOT ADMIN - Redirecting to login");
            return "redirect:/login";
        }
        
        System.out.println("✅ User is ADMIN");
        
        Product product = productService.getProductById(id);
        if (product == null) {
            System.out.println("❌ Product not found!");
            return "redirect:/admin/products";
        }
        
        System.out.println("✅ Product found: " + product.getName());
        System.out.println("   Price: " + product.getPrice());
        System.out.println("   Category: " + product.getCategory());
        
        model.addAttribute("product", product);
        model.addAttribute("username", session.getAttribute("username"));
        
        System.out.println("✅ Returning view: admin/product-form");
        System.out.println("========================================");
        
        return "admin/product-form";
    }
    
    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String category,
                               @RequestParam Double price,
                               @RequestParam(required = false) Double oldPrice,
                               @RequestParam String imageUrl,
                               @RequestParam Integer stock,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false, defaultValue = "false") Boolean featured,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        System.out.println("=== UPDATE PRODUCT ===");
        System.out.println("Product ID: " + id);
        
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
                return "redirect:/admin/products";
            }
            
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setOldPrice(oldPrice);
            product.setImageUrl(imageUrl);
            product.setStock(stock);
            product.setDescription(description);
            product.setFeatured(featured);
            
            productService.saveProduct(product);
            System.out.println("Product updated successfully!");
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
}