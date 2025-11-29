package com.taphoa.repository;

import com.taphoa.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByFeaturedTrue();
    List<Product> findByNameContaining(String keyword);
    @Query("SELECT p FROM Product p WHERE p.oldPrice IS NOT NULL AND p.oldPrice > p.price")
    List<Product> findDiscountedProducts();
}
