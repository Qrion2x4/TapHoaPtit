package com.taphoa.controller;

import com.taphoa.entity.Order;
import com.taphoa.entity.Product;
import com.taphoa.entity.User;
import com.taphoa.repository.OrderRepository;
import com.taphoa.service.OrderService;
import com.taphoa.service.ProductService;
import com.taphoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalTime;

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

    /**
     * API lấy thống kê doanh thu và doanh số theo khoảng ngày
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpSession session) {

        if (!isAdmin(session)) {
            return Map.of("error", "Unauthorized");
        }

        // Mặc định: 30 ngày gần nhất
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(30);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Object> stats = new HashMap<>();

        // Doanh thu tổng
        Double totalRevenue = orderRepository.getTotalRevenueByDateRange(startDateTime, endDateTime);
        Long totalProductsSold = orderRepository.getTotalProductsSoldByDateRange(startDateTime, endDateTime);

        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        stats.put("totalProductsSold", totalProductsSold != null ? totalProductsSold : 0);
        stats.put("startDate", startDate.toString());
        stats.put("endDate", endDate.toString());

        // Doanh thu theo ngày
        List<Object[]> dailyData = orderRepository.getRevenueByDay(startDateTime, endDateTime);
        List<Map<String, Object>> dailyStats = dailyData.stream().map(row -> {
            Map<String, Object> day = new HashMap<>();
            day.put("date", row[0].toString());
            day.put("revenue", row[1]);
            day.put("orderCount", row[2]);
            return day;
        }).collect(Collectors.toList());
        stats.put("dailyRevenue", dailyStats);

        // Doanh số bán theo sản phẩm
        List<Object[]> productSales = orderRepository.getProductSalesStatistics(startDateTime, endDateTime);
        List<Map<String, Object>> productStats = productSales.stream().map(row -> {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", row[0]);
            product.put("productName", row[1]);
            product.put("quantitySold", row[2]);
            product.put("revenue", row[3]);
            return product;
        }).collect(Collectors.toList());
        stats.put("productSales", productStats);

        // Thống kê đơn hàng theo trạng thái
        List<Object[]> orderStatus = orderRepository.getOrderCountByStatus(startDateTime, endDateTime);
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : orderStatus) {
            statusStats.put((String) row[0], ((Number) row[1]).longValue());
        }
        stats.put("orderByStatus", statusStats);

        return stats;
    }

    /**
     * API lấy doanh thu theo tháng
     */
    @GetMapping("/api/monthly-statistics")
    @ResponseBody
    public Map<String, Object> getMonthlyStatistics(
            @RequestParam(required = false) Integer year,
            HttpSession session) {

        if (!isAdmin(session)) {
            return Map.of("error", "Unauthorized");
        }

        if (year == null) year = LocalDate.now().getYear();

        LocalDateTime startDateTime = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);

        Map<String, Object> stats = new HashMap<>();
        stats.put("year", year);

        // Doanh thu theo tháng
        List<Object[]> monthlyData = orderRepository.getRevenueByMonth(startDateTime, endDateTime);
        List<Map<String, Object>> monthlyStats = monthlyData.stream().map(row -> {
            Map<String, Object> month = new HashMap<>();
            month.put("month", String.format("%d/%d", ((Number) row[1]).intValue(), ((Number) row[0]).intValue()));
            month.put("revenue", row[2]);
            month.put("orderCount", row[3]);
            return month;
        }).collect(Collectors.toList());

        stats.put("monthlyRevenue", monthlyStats);

        // Tổng doanh thu cả năm
        Double totalRevenue = monthlyStats.stream()
                .mapToDouble(m -> ((Number) m.get("revenue")).doubleValue())
                .sum();
        stats.put("totalRevenue", totalRevenue);

        return stats;
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
                                    @RequestParam("status") String status,
                                    RedirectAttributes redirectAttributes) {

        try {
            orderService.updateStatusAndHandleStock(id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật trạng thái đơn #" + id + " thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cập nhật trạng thái: " + e.getMessage());
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



    @PostMapping("/orders/{id}/approve-cancel")
    public String approveCancelOrder(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        try {
            orderService.approveCancel(id); // <--- Gọi Service
            redirectAttributes.addFlashAttribute("success", "Đã duyệt hủy đơn hàng #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // 2. TỪ CHỐI YÊU CẦU HỦY (GỌI SERVICE)
    @PostMapping("/orders/{id}/reject-cancel")
    public String rejectCancelOrder(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        try {
            orderService.rejectCancel(id); // <--- Gọi Service
            redirectAttributes.addFlashAttribute("success", "Đã từ chối yêu cầu hủy đơn #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }



}