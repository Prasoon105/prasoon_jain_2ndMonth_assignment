package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;

import java.time.LocalDateTime;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    public Order updateOrder(Long id, Order order) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        existingOrder.setStatus(order.getStatus());
        return orderRepository.save(existingOrder);
    }
    
    public void cancelOrder(Long id) {
        orderRepository.deleteById(id);
    }
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public Order placeOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        Order newOrder = orderRepository.save(order);
        
        // Send message to Kafka
        kafkaTemplate.send("order-topic", "Order placed: " + newOrder.getOrderId());

        return newOrder;
    }
    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

}
