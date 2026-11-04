package com.logistics.service;

import com.logistics.model.*;
import com.logistics.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final RouteRepository routeRepository;
    private final OrderRepository orderRepository;
    private final OptimizationService optimizationService;

    public DataInitializer(UserRepository userRepository,
                           WarehouseRepository warehouseRepository,
                           ProductRepository productRepository,
                           InventoryRepository inventoryRepository,
                           RouteRepository routeRepository,
                           OrderRepository orderRepository,
                           OptimizationService optimizationService) {
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.routeRepository = routeRepository;
        this.orderRepository = orderRepository;
        this.optimizationService = optimizationService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return; // Already seeded

        // 1. Seed Users
        User admin = userRepository.save(new User("admin", "admin123", "Chief Logistics Admin", "ADMIN", "admin@logistics.in"));
        User user1 = userRepository.save(new User("user", "user123", "Aarav Sharma (Procurement)", "USER", "aarav@enterprise.in"));
        User user2 = userRepository.save(new User("sarah", "sarah123", "Priya Patel (Operations)", "USER", "priya@supplycorp.in"));

        // 2. Seed Warehouses in Major Indian Logistics Hubs
        Warehouse whMum = warehouseRepository.save(new Warehouse("WH-MUM", "Mumbai Central Hub", "Mumbai", "Bhiwandi Logistics Park, Mumbai, Maharashtra", 10000, 19.0760, 72.8777));
        Warehouse whDel = warehouseRepository.save(new Warehouse("WH-DEL", "Delhi NCR Hub", "Delhi", "NH48 Freight Corridor, Gurugram, Delhi NCR", 9500, 28.6139, 77.2090));
        Warehouse whBlr = warehouseRepository.save(new Warehouse("WH-BLR", "Bengaluru South Hub", "Bengaluru", "Hosur Road Logistics Zone, Bengaluru, Karnataka", 8500, 12.9716, 77.5946));
        Warehouse whCcu = warehouseRepository.save(new Warehouse("WH-CCU", "Kolkata Eastern Hub", "Kolkata", "Dankuni Industrial Hub, Kolkata, West Bengal", 7000, 22.5726, 88.3639));
        Warehouse whHyd = warehouseRepository.save(new Warehouse("WH-HYD", "Hyderabad Deccan Hub", "Hyderabad", "Shamshabad Cargo Terminal, Hyderabad, Telangana", 8000, 17.3850, 78.4867));

        // 3. Seed Products
        Product pMicro = productRepository.save(new Product("PRD-ELEC-01", "Industrial Microcontrollers", "Electronics", "ARM Cortex-M7 Industrial control units", 450.0, 0.4, 25));
        Product pBattery = productRepository.save(new Product("PRD-BAT-02", "High-Capacity Li-Ion Batteries", "Energy Storage", "48V 100Ah Modular Energy Storage Pack", 850.0, 2.5, 20));
        Product pVaccine = productRepository.save(new Product("PRD-MED-03", "Temperature-Controlled Vaccines", "Pharmaceuticals", "Cold-chain pharmaceutical vials", 1200.0, 0.2, 15));
        Product pInverter = productRepository.save(new Product("PRD-AUTO-04", "EV Power Inverters", "Automotive", "High-efficiency silicon carbide traction inverters", 2100.0, 4.2, 10));
        Product pSolar = productRepository.save(new Product("PRD-SOLAR-05", "Solar Micro-Inverters", "Renewable Energy", "Grid-tied solar power convertors", 1500.0, 3.1, 15));
        Product pRations = productRepository.save(new Product("PRD-FOOD-06", "Organic Packaged Rations", "Food & Beverage", "Long shelf-life emergency nutrient packs", 180.0, 1.2, 40));

        // 4. Seed Inventories across Indian Warehouses
        // Mumbai Hub
        inventoryRepository.save(new Inventory(whMum, pMicro, 120));
        inventoryRepository.save(new Inventory(whMum, pBattery, 80));
        inventoryRepository.save(new Inventory(whMum, pVaccine, 18));
        inventoryRepository.save(new Inventory(whMum, pInverter, 45));
        inventoryRepository.save(new Inventory(whMum, pSolar, 60));
        inventoryRepository.save(new Inventory(whMum, pRations, 300));

        // Delhi Hub
        inventoryRepository.save(new Inventory(whDel, pMicro, 90));
        inventoryRepository.save(new Inventory(whDel, pBattery, 12)); // LOW STOCK
        inventoryRepository.save(new Inventory(whDel, pVaccine, 50));
        inventoryRepository.save(new Inventory(whDel, pInverter, 30));
        inventoryRepository.save(new Inventory(whDel, pRations, 200));

        // Bengaluru Hub
        inventoryRepository.save(new Inventory(whBlr, pMicro, 75));
        inventoryRepository.save(new Inventory(whBlr, pBattery, 65));
        inventoryRepository.save(new Inventory(whBlr, pVaccine, 35));
        inventoryRepository.save(new Inventory(whBlr, pSolar, 40));
        inventoryRepository.save(new Inventory(whBlr, pRations, 180));

        // Kolkata Hub
        inventoryRepository.save(new Inventory(whCcu, pMicro, 50));
        inventoryRepository.save(new Inventory(whCcu, pBattery, 40));
        inventoryRepository.save(new Inventory(whCcu, pVaccine, 20));
        inventoryRepository.save(new Inventory(whCcu, pSolar, 45));

        // Hyderabad Hub
        inventoryRepository.save(new Inventory(whHyd, pMicro, 110));
        inventoryRepository.save(new Inventory(whHyd, pBattery, 95));
        inventoryRepository.save(new Inventory(whHyd, pInverter, 55));
        inventoryRepository.save(new Inventory(whHyd, pSolar, 70));
        inventoryRepository.save(new Inventory(whHyd, pRations, 8)); // LOW STOCK

        // 5. Seed Routes across Indian Cities (Transshipment & Delivery)
        List<Route> routes = Arrays.asList(
                // Inter-Warehouse National Corridors
                new Route("RT-MUM-DEL", whMum.getName(), whMum.getId(), whDel.getName(), whDel.getId(), "WAREHOUSE_TO_WAREHOUSE", 14.0, 150, 1420, 24, true),
                new Route("RT-DEL-MUM", whDel.getName(), whDel.getId(), whMum.getName(), whMum.getId(), "WAREHOUSE_TO_WAREHOUSE", 14.5, 140, 1420, 24, true),
                new Route("RT-MUM-BLR", whMum.getName(), whMum.getId(), whBlr.getName(), whBlr.getId(), "WAREHOUSE_TO_WAREHOUSE", 11.0, 120, 980, 18, true),
                new Route("RT-MUM-HYD", whMum.getName(), whMum.getId(), whHyd.getName(), whHyd.getId(), "WAREHOUSE_TO_WAREHOUSE", 9.5, 130, 710, 14, true),
                new Route("RT-DEL-CCU", whDel.getName(), whDel.getId(), whCcu.getName(), whCcu.getId(), "WAREHOUSE_TO_WAREHOUSE", 15.0, 110, 1530, 26, true),
                new Route("RT-HYD-BLR", whHyd.getName(), whHyd.getId(), whBlr.getName(), whBlr.getId(), "WAREHOUSE_TO_WAREHOUSE", 8.0, 140, 570, 10, true),
                new Route("RT-BLR-HYD", whBlr.getName(), whBlr.getId(), whHyd.getName(), whHyd.getId(), "WAREHOUSE_TO_WAREHOUSE", 8.5, 120, 570, 10, true),
                new Route("RT-CCU-HYD", whCcu.getName(), whCcu.getId(), whHyd.getName(), whHyd.getId(), "WAREHOUSE_TO_WAREHOUSE", 13.0, 90, 1480, 25, true),

                // Warehouse to Indian Delivery Destinations
                new Route("RT-BLR-MAA", whBlr.getName(), whBlr.getId(), "Chennai Retail Center", null, "WAREHOUSE_TO_DESTINATION", 6.0, 160, 345, 6, true),
                new Route("RT-HYD-MAA", whHyd.getName(), whHyd.getId(), "Chennai Retail Center", null, "WAREHOUSE_TO_DESTINATION", 10.5, 100, 630, 11, true),
                new Route("RT-MUM-PUN", whMum.getName(), whMum.getId(), "Pune Fulfillment Center", null, "WAREHOUSE_TO_DESTINATION", 4.5, 180, 150, 3, true),
                new Route("RT-HYD-PUN", whHyd.getName(), whHyd.getId(), "Pune Fulfillment Center", null, "WAREHOUSE_TO_DESTINATION", 9.0, 95, 560, 10, true),
                new Route("RT-MUM-AMD", whMum.getName(), whMum.getId(), "Ahmedabad Distribution Depot", null, "WAREHOUSE_TO_DESTINATION", 7.5, 130, 530, 9, true),
                new Route("RT-DEL-AMD", whDel.getName(), whDel.getId(), "Ahmedabad Distribution Depot", null, "WAREHOUSE_TO_DESTINATION", 11.0, 85, 935, 16, true),
                new Route("RT-DEL-JAI", whDel.getName(), whDel.getId(), "Jaipur Metro Depot", null, "WAREHOUSE_TO_DESTINATION", 5.5, 150, 280, 5, true),
                new Route("RT-MUM-JAI", whMum.getName(), whMum.getId(), "Jaipur Metro Depot", null, "WAREHOUSE_TO_DESTINATION", 13.5, 70, 1150, 20, true)
        );
        routeRepository.saveAll(routes);

        // 6. Seed Sample Orders with Indian Cities
        // Order 1: Ready for optimization to Chennai Retail Center
        Order o1 = new Order("ORD-9801", user1, pMicro, 75, "DELIVERY", null, null, "Chennai Retail Center");
        orderRepository.save(o1);

        // Order 2: Optimized delivery order to Pune Fulfillment Center
        Order o2 = new Order("ORD-9802", user2, pBattery, 60, "DELIVERY", null, null, "Pune Fulfillment Center");
        orderRepository.save(o2);
        optimizationService.optimizeOrder(o2.getId());

        // Order 3: Inter-warehouse stock transfer request from Mumbai to Bengaluru
        Order o3 = new Order("ORD-9803", admin, pVaccine, 25, "TRANSFER", whMum, whBlr, null);
        orderRepository.save(o3);
        optimizationService.optimizeOrder(o3.getId());
    }
}
