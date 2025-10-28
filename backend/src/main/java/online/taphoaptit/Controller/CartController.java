package online.taphoaptit.controller;

import org.springframework.web.bind.annotation.*;
import online.taphoaptit.model.CartItem;
import java.util.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final List<CartItem> cart = new ArrayList<>();

    @GetMapping
    public List<CartItem> getCart() {
        return cart;
    }

    @PostMapping("/add")
    public List<CartItem> addItem(@RequestBody CartItem item) {
        for (CartItem i : cart) {
            if (i.getName().equals(item.getName())) {
                i.setQuantity(i.getQuantity() + item.getQuantity());
                return cart;
            }
        }
        cart.add(item);
        return cart;
    }

    @PostMapping("/remove")
    public List<CartItem> removeItem(@RequestBody CartItem item) {
        cart.removeIf(i -> i.getName().equals(item.getName()));
        return cart;
    }

    @PostMapping("/clear")
    public List<CartItem> clearCart() {
        cart.clear();
        return cart;
    }
}
