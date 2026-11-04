package com.logistics.controller;

import com.logistics.dto.CreateOrderDto;
import com.logistics.dto.TransferRequestDto;
import com.logistics.model.Inventory;
import com.logistics.model.Order;
import com.logistics.model.User;
import com.logistics.model.Warehouse;
import com.logistics.repository.InventoryRepository;
import com.logistics.repository.UserRepository;
import com.logistics.repository.WarehouseRepository;
import com.logistics.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {

    private final OrderService orderService;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    public TransferController(OrderService orderService,
                              InventoryRepository inventoryRepository,
                              WarehouseRepository warehouseRepository,
                              UserRepository userRepository) {
        this.orderService = orderService;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Identifies candidate source warehouses capable of supplying the requested quantity.
     */
    @GetMapping("/candidates")
    public ResponseEntity<?> getTransferCandidates(
            @RequestParam Long productId,
            @RequestParam Long destinationWarehouseId) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        List<Map<String, Object>> candidates = new ArrayList<>();

        for (Inventory inv : inventories) {
            Warehouse wh = inv.getWarehouse();
            if (!wh.getId().equals(destinationWarehouseId) && inv.getAvailableQuantity() > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("warehouseId", wh.getId());
                map.put("warehouseName", wh.getName());
                map.put("city", wh.getCity());
                map.put("availableQuantity", inv.getAvailableQuantity());
                candidates.add(map);
            }
        }

        return ResponseEntity.ok(candidates);
    }

    /**
     * Initiates and immediately optimizes an inter-warehouse stock transfer using MCMF.
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeTransfer(@RequestBody TransferRequestDto dto) {
        try {
            // Use admin account as initiator
            User admin = userRepository.findByUsername("admin")
                    .orElseGet(() -> userRepository.findAll().stream()
                            .filter(u -> "ADMIN".equals(u.getRole()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Admin user missing")));

            CreateOrderDto orderDto = new CreateOrderDto();
            orderDto.setUserId(admin.getId());
            orderDto.setProductId(dto.getProductId());
            orderDto.setQuantity(dto.getQuantity());
            orderDto.setOrderType("TRANSFER");
            orderDto.setSourceWarehouseId(dto.getSourceWarehouseId()); // can be null for auto-select
            orderDto.setDestinationWarehouseId(dto.getDestinationWarehouseId());

            Order order = orderService.createOrder(orderDto);
            Order optimizedOrder = orderService.optimizeOrder(order.getId());

            return ResponseEntity.ok(optimizedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
