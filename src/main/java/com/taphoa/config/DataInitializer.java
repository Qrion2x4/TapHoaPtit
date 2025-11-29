package com.taphoa.config;

import com.taphoa.entity.Product;
import com.taphoa.entity.User;
import com.taphoa.repository.ProductRepository;
import com.taphoa.repository.UserRepository;
import com.taphoa.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CouponService couponService;
    
    @Override
    public void run(String... args) {
        System.out.println("===========================================");
        System.out.println("🚀 Starting Data Initialization...");
        System.out.println("===========================================");
        
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // ============ TÀI KHOẢN ADMIN ĐỂ TEST ============
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@taphoaptit.online");
            admin.setFullName("Administrator");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            
            System.out.println("✓ Created ADMIN account:");
            System.out.println("  - Username: admin");
            System.out.println("  - Password: admin123");
            System.out.println("  - Access: http://localhost:8080/admin");
        } else {
            System.out.println("✓ Admin account already exists");
        }
        
        System.out.println("-------------------------------------------");
        
        // ============ MÃ GIẢM GIÁ - NEWBIE ============
        couponService.createNewbieCoupon();
        System.out.println("✓ NEWBIE coupon ready (10% off, max 50k)");
        
        System.out.println("-------------------------------------------");
        
        // ============ TẠO SẢN PHẨM MẪU ============
        if (productRepository.count() == 0) {
            System.out.println("📦 Creating sample products...");
            
            addProduct("Gạo ST25 túi 5kg", 119000.0, 135000.0, "🌾", "Thực phẩm", true);
            addProduct("Trứng gà tươi (10 quả)", 34000.0, null, "🥚", "Thực phẩm", true);
            addProduct("Sữa tươi Vinamilk 1L", 39000.0, null, "🥛", "Đồ uống", true);
            addProduct("Bánh mì sandwich", 26000.0, null, "🍞", "Thực phẩm", true);
            addProduct("Dầu ăn Simply 1L", 44000.0, null, "🥫", "Gia vị", true);
            addProduct("Muối I-ốt 500g", 8000.0, null, "🧂", "Gia vị", true);
            addProduct("Mì gói Hảo Hảo (30 gói)", 82000.0, null, "🍜", "Thực phẩm", true);
            addProduct("Cà phê G7 hòa tan (16 gói)", 69000.0, null, "☕", "Đồ uống", true);
            addProduct("Nước cam Vinamilk 1L", 22000.0, null, "🧃", "Đồ uống", false);
            addProduct("Coca Cola lon 330ml (6 lon)", 36000.0, null, "🥤", "Đồ uống", false);
            addProduct("Nước suối Aquafina 1.5L", 13000.0, null, "🧊", "Đồ uống", false);
            addProduct("Trà xanh 0 độ+ 450ml", 18000.0, null, "🍵", "Đồ uống", false);
            addProduct("Nước mắm Nam Ngư 500ml", 25000.0, null, "🍶", "Gia vị", false);
            addProduct("Tương ớt Cholimex 270g", 20000.0, null, "🌶️", "Gia vị", false);
            addProduct("Bột canh 200g", 15000.0, null, "🧂", "Gia vị", false);
            addProduct("Kem đánh răng P/S 230g", 34500.0, null, "🦷", "Chăm sóc cá nhân", false);
            addProduct("Dầu gội Clear 650ml", 148000.0, null, "🧴", "Chăm sóc cá nhân", false);
            addProduct("Xà phòng Lifebuoy 90g", 11000.0, null, "🧼", "Chăm sóc cá nhân", false);
            
            long totalProducts = productRepository.count();
            System.out.println("-------------------------------------------");
            System.out.println("✅ Total products created: " + totalProducts);
        } else {
            System.out.println("✓ Products already exist in database");
            System.out.println("  Total products: " + productRepository.count());
        }
        
        System.out.println("===========================================");
        System.out.println("✅ Data Initialization Completed!");
        System.out.println("===========================================");
        System.out.println();
        System.out.println("📌 LOGIN CREDENTIALS:");
        System.out.println("   Admin: admin / admin123");
        System.out.println("   URL: http://localhost:8080/admin");
        System.out.println();
        System.out.println("🎁 COUPON CODE:");
        System.out.println("   NEWBIE - Giảm 10% (tối đa 50k, đơn tối thiểu 100k)");
        System.out.println();
        System.out.println("🏠 Website: http://localhost:8080");
        System.out.println("===========================================");
    }
    
    private void addProduct(String name, Double price, Double oldPrice, 
                           String imageUrl, String category, Boolean featured) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setOldPrice(oldPrice);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setStock(100);
        p.setFeatured(featured);
        productRepository.save(p);
    }
}