package com.taphoa.service;

import com.taphoa.entity.Order;
import com.taphoa.entity.OrderLog;
import com.taphoa.repository.OrderLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderLogService {

    @Autowired
    private OrderLogRepository orderLogRepo;

    public void log(Order order, String action, String actor, String description) {
        OrderLog log = new OrderLog();
        log.setOrder(order);
        log.setAction(action);
        log.setActor(actor);
        log.setDescription(description);
        orderLogRepo.save(log);
    }

    public List<OrderLog> getLogs(Long orderId) {
        return orderLogRepo.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}
