package com.taphoa.controller;

import com.taphoa.entity.Order;
import com.taphoa.entity.Product;
import com.taphoa.entity.User;
import com.taphoa.repository.OrderRepository;
import com.taphoa.service.OrderService;
import com.taphoa.service.ProductService;
import com.taphoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
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

    @Autowired
    private OrderRepository orderRepository;

    private boolean isAdmin(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return false;

        User user = userService.getUserById(userId);
        return user != null && "ADMIN".equals(user.getRole());
    }

    private String saveImage(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return null;
        }
        try {
            String fileName = new Date().getTime() + "_" + imageFile.getOriginalFilename();

            Path uploadPath = Paths.get("C:/taphoa-images/");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = imageFile.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

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

    @GetMapping("/orders")
    public String manageOrders(HttpSession session, Model model,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) List<String> status,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        if (status != null && status.isEmpty()) {
            status = null;
        }

        List<Order> orders = orderRepository.searchOrders(keyword, status, startDateTime, endDateTime);

        model.addAttribute("orders", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status); // Trả về list để frontend tick lại các ô đã chọn
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

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

            Order order = orderRepository.findById(id).orElse(null);

            if (order != null) {

                order.setStatus(status);


                orderRepository.save(order);

                System.out.println("Đã cập nhật đơn hàng #" + id + " sang trạng thái: " + status);
                redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
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
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam Integer stock,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false, defaultValue = "false") Boolean featured,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setOldPrice(oldPrice);

            String fileName = saveImage(imageFile);
            if (fileName != null) {
                product.setImageUrl(fileName);
            } else {
                product.setImageUrl("default.jpg");
            }

            product.setStock(stock);
            product.setDescription(description);
            product.setFeatured(featured);

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("username", session.getAttribute("username"));

        return "admin/product-form";
    }

    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String category,
                                @RequestParam Double price,
                                @RequestParam(required = false) Double oldPrice,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                @RequestParam("imageUrl") String oldImageUrl,
                                @RequestParam Integer stock,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false, defaultValue = "false") Boolean featured,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
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

            if (!imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                if (fileName != null) {
                    product.setImageUrl(fileName);
                }
            } else {
                product.setImageUrl(oldImageUrl);
            }

            product.setStock(stock);
            product.setDescription(description);
            product.setFeatured(featured);

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
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