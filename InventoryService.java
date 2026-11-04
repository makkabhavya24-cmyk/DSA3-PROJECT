package com.logistics.service;

import com.logistics.model.Inventory;
import com.logistics.model.Product;
import com.logistics.model.Warehouse;
import com.logistics.repository.InventoryRepository;
import com.logistics.repository.ProductRepository;
import com.logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            WarehouseRepository warehouseRepository,
                            ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
    }

    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getInventoriesByWarehouse(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    public List<Inventory> getLowStockInventories() {
        return inventoryRepository.findLowStockInventories();
    }

    @Transactional
    public Inventory assignProductToWarehouse(Long warehouseId, Long productId, int initialQuantity) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + warehouseId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        return inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + initialQuantity);
                    return inventoryRepository.save(existing);
                })
                .orElseGet(() -> {
                    Inventory inv = new Inventory(warehouse, product, initialQuantity);
                    return inventoryRepository.save(inv);
                });
    }

    @Transactional
    public Inventory adjustStockQuantity(Long inventoryId, int deltaQuantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

        int newQuantity = inventory.getQuantity() + deltaQuantity;
        if (newQuantity < inventory.getReservedQuantity()) {
            throw new IllegalArgumentException(String.format("Cannot reduce stock below reserved amount (%d). Attempted new quantity: %d",
                    inventory.getReservedQuantity(), newQuantity));
        }

        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }
}
