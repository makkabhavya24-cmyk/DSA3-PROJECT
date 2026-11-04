package com.logistics.service;

import com.logistics.model.Route;
import com.logistics.model.Warehouse;
import com.logistics.repository.InventoryRepository;
import com.logistics.repository.RouteRepository;
import com.logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final RouteRepository routeRepository;
    private final InventoryRepository inventoryRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            RouteRepository routeRepository,
                            InventoryRepository inventoryRepository) {
        this.warehouseRepository = warehouseRepository;
        this.routeRepository = routeRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + id));
    }

    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse) {
        if (warehouseRepository.existsByCode(warehouse.getCode())) {
            throw new IllegalArgumentException("Warehouse code already exists: " + warehouse.getCode());
        }
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(Long id, Warehouse updated) {
        Warehouse existing = getWarehouseById(id);
        existing.setName(updated.getName());
        existing.setCity(updated.getCity());
        existing.setAddress(updated.getAddress());
        existing.setStorageCapacity(updated.getStorageCapacity());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        return warehouseRepository.save(existing);
    }

    public Map<String, Object> getWarehouseConnections(Long warehouseId) {
        Warehouse warehouse = getWarehouseById(warehouseId);
        List<Route> outgoing = routeRepository.findBySourceName(warehouse.getName());
        List<Route> incoming = routeRepository.findByDestinationName(warehouse.getName());

        Map<String, Object> connections = new HashMap<>();
        connections.put("warehouse", warehouse);
        connections.put("outgoingRoutes", outgoing);
        connections.put("incomingRoutes", incoming);
        connections.put("inventories", inventoryRepository.findByWarehouseId(warehouseId));
        return connections;
    }
}
