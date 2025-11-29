package com.taphoa.service;

import com.taphoa.entity.Product;
import com.taphoa.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }
    
    @Transactional
    public Product saveProduct(Product product) {
        System.out.println("Saving product: " + product.getName());
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        System.out.println("Deleting product ID: " + id);
        productRepository.deleteById(id);
    }

    public List<Product> getDiscountedProducts() {
        return productRepository.findDiscountedProducts();
    }
}