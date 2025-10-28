package online.taphoaptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import online.taphoaptit.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> { }
