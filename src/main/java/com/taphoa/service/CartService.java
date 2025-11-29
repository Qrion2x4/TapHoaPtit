package com.taphoa.service;

import com.taphoa.entity.CartItem;
import com.taphoa.entity.Product;
import com.taphoa.entity.User;
import com.taphoa.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductService productService;
    
    public List<CartItem> getCartItems(User user) {
        System.out.println("=== GET CART ITEMS ===");
        System.out.println("User: " + user.getUsername() + " (ID: " + user.getId() + ")");
        
        List<CartItem> items = cartItemRepository.findByUser(user);
        System.out.println("Found " + items.size() + " items");
        
        return items;
    }
    
    public int getCartCount(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        int count = items.stream().mapToInt(CartItem::getQuantity).sum();
        System.out.println("Cart count for " + user.getUsername() + ": " + count);
        return count;
    }
    

    public CartItem getCartItemById(Long id) {
        System.out.println("Getting cart item by ID: " + id);
        Optional<CartItem> item = cartItemRepository.findById(id);
        if (item.isPresent()) {
            System.out.println("✅ Found: " + item.get().getProduct().getName());
            return item.get();
        } else {
            System.out.println("❌ Not found");
            return null;
        }
    }
    
    @Transactional
    public void addToCart(User user, Long productId, Integer quantity) {
        System.out.println("=== CartService.addToCart ===");
        System.out.println("User: " + user.getUsername() + " (ID: " + user.getId() + ")");
        System.out.println("Product ID: " + productId);
        System.out.println("Quantity: " + quantity);
        
        Product product = productService.getProductById(productId);
        if (product == null) {
            System.out.println("ERROR: Product not found!");
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }
        
        System.out.println("Product found: " + product.getName());
        
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProductId(user, productId);
        
        if (existingItem.isPresent()) {
            System.out.println("Item already exists in cart, updating quantity");
            CartItem item = existingItem.get();
            int oldQuantity = item.getQuantity();
            item.setQuantity(oldQuantity + quantity);
            
            CartItem saved = cartItemRepository.saveAndFlush(item);
            System.out.println("Updated quantity from " + oldQuantity + " to " + saved.getQuantity());
        } else {
            System.out.println("Creating new cart item");
            CartItem item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(quantity);
            
            CartItem saved = cartItemRepository.saveAndFlush(item);
            System.out.println("Saved new cart item with ID: " + saved.getId());
        }
        
        // Verify
        List<CartItem> allItems = cartItemRepository.findByUser(user);
        System.out.println("Total items in cart now: " + allItems.size());
    }
    
    @Transactional
    public void removeFromCart(Long cartItemId) {
        System.out.println("Removing cart item ID: " + cartItemId);
        cartItemRepository.deleteById(cartItemId);
        cartItemRepository.flush();
        System.out.println("✅ Removed successfully");
    }
    
    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        System.out.println("Updating cart item ID: " + cartItemId + " to quantity: " + quantity);
        
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setQuantity(quantity);
            cartItemRepository.saveAndFlush(item);
            System.out.println("✅ Updated successfully");
        } else {
            System.out.println("❌ Cart item not found!");
            throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ!");
        }
    }
    
    @Transactional
    public void clearCart(User user) {
        System.out.println("Clearing cart for user: " + user.getUsername());
        cartItemRepository.deleteByUser(user);
        cartItemRepository.flush();
        System.out.println("✅ Cart cleared");
    }
    
    public Double getCartTotal(User user) {
        List<CartItem> items = getCartItems(user);
        double total = items.stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
        System.out.println("Cart total: " + total);
        return total;
    }
}