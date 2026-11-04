package com.logistics.controller;

import com.logistics.dto.StockAdjustDto;
import com.logistics.model.Inventory;
import com.logistics.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventories")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> getInventories(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false, defaultValue = "false") boolean lowStockOnly) {
        if (lowStockOnly) {
            return inventoryService.getLowStockInventories();
        }
        if (warehouseId != null) {
            return inventoryService.getInventoriesByWarehouse(warehouseId);
        }
        return inventoryService.getAllInventories();
    }

    @GetMapping("/low-stock")
    public List<Inventory> getLowStock() {
        return inventoryService.getLowStockInventories();
    }

    @PostMapping
    public ResponseEntity<?> assignProductToWarehouse(@RequestBody Map<String, Object> payload) {
        try {
            Long warehouseId = Long.valueOf(payload.get("warehouseId").toString());
            Long productId = Long.valueOf(payload.get("productId").toString());
            int quantity = payload.containsKey("quantity") ? Integer.parseInt(payload.get("quantity").toString()) : 0;
            return ResponseEntity.ok(inventoryService.assignProductToWarehouse(warehouseId, productId, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/adjust")
    public ResponseEntity<?> adjustStock(@PathVariable Long id, @RequestBody StockAdjustDto dto) {
        try {
            return ResponseEntity.ok(inventoryService.adjustStockQuantity(id, dto.getDeltaQuantity()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
