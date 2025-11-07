package com.taphoa.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double price;
    
    private Double oldPrice;
    
    private String imageUrl;
    
    @Column(length = 2000)
    private String description;
    
    private String category;
    
    private Integer stock = 0;
    
    private Boolean featured = false;
}