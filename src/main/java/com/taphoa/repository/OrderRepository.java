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

    // ===== THỐNG KÊ DOANH THU =====
    
    // Lấy tổng doanh thu theo khoảng ngày (chỉ tính đơn COMPLETED)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Double getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // Lấy tổng số sản phẩm bán được theo khoảng ngày
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "JOIN oi.order o WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long getTotalProductsSoldByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Lấy doanh thu theo ngày (dùng cho biểu đồ)
    @Query("SELECT DATE(o.createdAt) as date, COALESCE(SUM(o.totalAmount), 0) as revenue, COUNT(o) as orderCount " +
            "FROM Order o WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "GROUP BY DATE(o.createdAt) ORDER BY date ASC")
    List<Object[]> getRevenueByDay(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Lấy doanh thu theo tháng (dùng cho biểu đồ)
    @Query("SELECT YEAR(o.createdAt) as year, MONTH(o.createdAt) as month, " +
            "COALESCE(SUM(o.totalAmount), 0) as revenue, COUNT(o) as orderCount " +
            "FROM Order o WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) ORDER BY year ASC, month ASC")
    List<Object[]> getRevenueByMonth(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // Lấy doanh số bán theo sản phẩm
    @Query("SELECT p.id, p.name, COALESCE(SUM(oi.quantity), 0) as totalSold, " +
            "COALESCE(SUM(oi.price * oi.quantity), 0) as totalRevenue " +
            "FROM OrderItem oi JOIN oi.product p JOIN oi.order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "GROUP BY p.id, p.name ORDER BY totalSold DESC")
    List<Object[]> getProductSalesStatistics(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Lấy tổng số đơn hàng theo trạng thái trong khoảng ngày
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
            "WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "GROUP BY o.status")
    List<Object[]> getOrderCountByStatus(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}