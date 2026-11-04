package com.logistics.service;

import com.logistics.dto.CreateOrderDto;
import com.logistics.model.*;
import com.logistics.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final OptimizationService optimizationService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        WarehouseRepository warehouseRepository,
                        InventoryRepository inventoryRepository,
                        OptimizationService optimizationService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.optimizationService = optimizationService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    @Transactional
    public Order createOrder(CreateOrderDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));

        Warehouse sourceWarehouse = null;
        if (dto.getSourceWarehouseId() != null) {
            sourceWarehouse = warehouseRepository.findById(dto.getSourceWarehouseId())
                    .orElse(null);
        }

        Warehouse destWarehouse = null;
        if ("TRANSFER".equalsIgnoreCase(dto.getOrderType()) && dto.getDestinationWarehouseId() != null) {
            destWarehouse = warehouseRepository.findById(dto.getDestinationWarehouseId())
                    .orElseThrow(() -> new IllegalArgumentException("Destination warehouse not found"));
        }

        String orderNum = "ORD-" + (System.currentTimeMillis() % 1000000);

        Order order = new Order(
                orderNum,
                user,
                product,
                dto.getQuantity(),
                dto.getOrderType() != null ? dto.getOrderType().toUpperCase() : "DELIVERY",
                sourceWarehouse,
                destWarehouse,
                dto.getDestinationLocation()
        );

        return orderRepository.save(order);
    }

    @Transactional
    public Order optimizeOrder(Long orderId) {
        optimizationService.optimizeOrder(orderId);
        return getOrderById(orderId);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = getOrderById(orderId);
        String currentStatus = order.getStatus();
        String targetStatus = newStatus.toUpperCase();

        if (currentStatus.equals(targetStatus)) {
            return order;
        }

        // When transitioning to DISPATCHED, deduct product inventory from the source warehouses
        if ("DISPATCHED".equals(targetStatus) && !"DISPATCHED".equals(currentStatus) && !"IN_TRANSIT".equals(currentStatus) && !"DELIVERED".equals(currentStatus)) {
            deductInventoryForOrder(order);
        }

        // When transitioning to DELIVERED for an inter-warehouse TRANSFER, increment product inventory at the destination warehouse
        if ("DELIVERED".equals(targetStatus) && !"DELIVERED".equals(currentStatus)) {
            if ("TRANSFER".equalsIgnoreCase(order.getOrderType()) && order.getDestinationWarehouse() != null) {
                int deliveredUnits = order.getShipmentPlan() != null ?
                        order.getShipmentPlan().getFulfilledQuantity() : order.getQuantity();

                Warehouse destWh = order.getDestinationWarehouse();
                Product prod = order.getProduct();

                Inventory destInv = inventoryRepository.findByWarehouseIdAndProductId(destWh.getId(), prod.getId())
                        .orElseGet(() -> new Inventory(destWh, prod, 0));

                destInv.setQuantity(destInv.getQuantity() + deliveredUnits);
                inventoryRepository.save(destInv);
            }
        }

        order.setStatus(targetStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private void deductInventoryForOrder(Order order) {
        if (order.getShipmentPlan() == null || order.getShipmentPlan().getHops().isEmpty()) {
            // If no plan, deduct from source warehouse if set
            if (order.getSourceWarehouse() != null) {
                Inventory inv = inventoryRepository.findByWarehouseIdAndProductId(
                        order.getSourceWarehouse().getId(), order.getProduct().getId())
                        .orElseThrow(() -> new IllegalStateException("Insufficient inventory at source warehouse"));
                inv.setQuantity(Math.max(0, inv.getQuantity() - order.getQuantity()));
                inventoryRepository.save(inv);
            }
            return;
        }

        // Identify starting hops originating from warehouses
        List<Warehouse> warehouses = warehouseRepository.findAll();
        Map<String, Warehouse> nameToWarehouse = new HashMap<>();
        for (Warehouse w : warehouses) {
            nameToWarehouse.put(w.getName(), w);
        }

        // Group quantities by origin warehouse
        // To avoid double counting transshipments (e.g. WH1 -> WH2 -> Dest),
        // we track net flow pushed out of warehouse nodes
        Map<String, Integer> netOutflow = new HashMap<>();
        for (ShipmentHop hop : order.getShipmentPlan().getHops()) {
            netOutflow.put(hop.getFromNode(), netOutflow.getOrDefault(hop.getFromNode(), 0) + hop.getQuantity());
            netOutflow.put(hop.getToNode(), netOutflow.getOrDefault(hop.getToNode(), 0) - hop.getQuantity());
        }

        for (Map.Entry<String, Integer> entry : netOutflow.entrySet()) {
            if (entry.getValue() > 0 && nameToWarehouse.containsKey(entry.getKey())) {
                Warehouse originWh = nameToWarehouse.get(entry.getKey());
                int unitsDrawn = entry.getValue();

                Inventory inv = inventoryRepository.findByWarehouseIdAndProductId(originWh.getId(), order.getProduct().getId())
                        .orElseThrow(() -> new IllegalStateException("Inventory record missing for warehouse: " + originWh.getName()));

                inv.setQuantity(Math.max(0, inv.getQuantity() - unitsDrawn));
                inventoryRepository.save(inv);
            }
        }
    }
}
