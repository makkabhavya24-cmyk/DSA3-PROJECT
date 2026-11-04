package com.logistics.controller;

import com.logistics.model.Inventory;
import com.logistics.model.Order;
import com.logistics.model.Route;
import com.logistics.model.Warehouse;
import com.logistics.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/network")
@CrossOrigin(origins = "*")
public class NetworkController {

    private final WarehouseRepository warehouseRepository;
    private final RouteRepository routeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public NetworkController(WarehouseRepository warehouseRepository,
                             RouteRepository routeRepository,
                             ProductRepository productRepository,
                             InventoryRepository inventoryRepository,
                             OrderRepository orderRepository) {
        this.warehouseRepository = warehouseRepository;
        this.routeRepository = routeRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getNetworkSummary() {
        Map<String, Object> summary = new HashMap<>();

        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<Route> routes = routeRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<Inventory> inventories = inventoryRepository.findAll();

        int totalPhysicalStock = inventories.stream().mapToInt(Inventory::getQuantity).sum();
        long lowStockCount = inventories.stream().filter(Inventory::isLowStock).count();
        long activeRoutesCount = routes.stream().filter(Route::isActive).count();
        long pendingOrdersCount = orders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();
        long optimizedOrdersCount = orders.stream().filter(o -> "OPTIMIZED".equalsIgnoreCase(o.getStatus())).count();

        summary.put("totalWarehouses", warehouses.size());
        summary.put("totalProducts", productRepository.count());
        summary.put("totalPhysicalStock", totalPhysicalStock);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalRoutes", routes.size());
        summary.put("activeRoutesCount", activeRoutesCount);
        summary.put("totalOrders", orders.size());
        summary.put("pendingOrdersCount", pendingOrdersCount);
        summary.put("optimizedOrdersCount", optimizedOrdersCount);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getGraphStructure() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<Route> routes = routeRepository.findAll();

        // Unique nodes
        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();

        // Add warehouses
        for (Warehouse w : warehouses) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", w.getName());
            node.put("label", w.getName());
            node.put("type", "WAREHOUSE");
            node.put("code", w.getCode());
            node.put("city", w.getCity());
            node.put("capacity", w.getStorageCapacity());
            node.put("lat", w.getLatitude());
            node.put("lon", w.getLongitude());
            nodes.put(w.getName(), node);
        }

        // Add destinations referenced by routes
        for (Route r : routes) {
            if (!nodes.containsKey(r.getDestinationName())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", r.getDestinationName());
                node.put("label", r.getDestinationName());
                node.put("type", "DESTINATION");
                node.put("city", r.getDestinationName());
                nodes.put(r.getDestinationName(), node);
            }
        }

        List<Map<String, Object>> edges = new ArrayList<>();
        for (Route r : routes) {
            Map<String, Object> edge = new HashMap<>();
            edge.put("id", r.getId());
            edge.put("code", r.getRouteCode());
            edge.put("source", r.getSourceName());
            edge.put("target", r.getDestinationName());
            edge.put("cost", r.getUnitCost());
            edge.put("capacity", r.getCapacity());
            edge.put("distanceKm", r.getDistanceKm());
            edge.put("travelTimeHours", r.getTravelTimeHours());
            edge.put("active", r.isActive());
            edge.put("type", r.getRouteType());
            edges.add(edge);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("nodes", new ArrayList<>(nodes.values()));
        response.put("edges", edges);

        return ResponseEntity.ok(response);
    }
}
