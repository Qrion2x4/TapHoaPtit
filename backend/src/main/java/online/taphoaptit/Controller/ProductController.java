package online.taphoaptit.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import online.taphoaptit.entity.Product;
import online.taphoaptit.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Product> getAll() {
        return repo.findAll();
    }

    @PostMapping("/add")
    public String addProduct(@RequestBody Product p) {
        repo.save(p);
        return "Thêm sản phẩm thành công!";
    }
}
