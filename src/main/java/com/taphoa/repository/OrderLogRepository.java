package com.taphoa.repository;

import com.taphoa.entity.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {

    List<OrderLog> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
