package com.logistics.repository;

import com.logistics.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByWarehouseId(Long warehouseId);
    List<Inventory> findByProductId(Long productId);
    Optional<Inventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reservedQuantity) <= i.product.minStockThreshold")
    List<Inventory> findLowStockInventories();
}
