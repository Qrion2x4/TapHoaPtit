package online.taphoaptit.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import online.taphoaptit.entity.*;
import online.taphoaptit.repository.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public CartController(CartItemRepository cartRepo, ProductRepository productRepo, UserRepository userRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    // ✅ Xem giỏ hàng theo user
    @GetMapping("/{username}")
    public List<CartItem> getCart(@PathVariable String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) return List.of();
        return cartRepo.findByUser(user);
    }

    // ✅ Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public String addToCart(@RequestParam String username,
                            @RequestParam Long productId,
                            @RequestParam int quantity) {
        User user = userRepo.findByUsername(username);
        Product product = productRepo.findById(productId).orElse(null);

        if (user == null || product == null)
            return "User hoặc sản phẩm không tồn tại!";

        CartItem item = new CartItem(user, product, quantity);
        cartRepo.save(item);
        return "Đã thêm sản phẩm vào giỏ hàng!";
    }

    // ✅ Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/{id}")
    public String removeItem(@PathVariable Long id) {
        cartRepo.deleteById(id);
        return "Đã xóa sản phẩm khỏi giỏ hàng!";
    }
}
