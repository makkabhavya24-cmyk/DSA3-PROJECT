package com.logistics.controller;

import com.logistics.dto.CreateOrderDto;
import com.logistics.model.Order;
import com.logistics.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getOrders(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return orderService.getUserOrders(userId);
        }
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderDto dto) {
        try {
            return ResponseEntity.ok(orderService.createOrder(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/optimize")
    public ResponseEntity<?> optimizeOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.optimizeOrder(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status field is required"));
            }
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
