package com.taphoa.repository;

import com.taphoa.entity.Order;
import com.taphoa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR CAST(o.id AS string) LIKE %:keyword% OR o.phone LIKE %:keyword% OR o.user.username LIKE %:keyword%) " +
            "AND (:statuses IS NULL OR o.status IN :statuses) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.id DESC")
    List<Order> searchOrders(@Param("keyword") String keyword,
                             @Param("statuses") List<String> statuses,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
}