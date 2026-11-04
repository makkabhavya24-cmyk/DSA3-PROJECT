package com.logistics.service;

import com.logistics.algorithm.FlowEdge;
import com.logistics.algorithm.FlowNetwork;
import com.logistics.algorithm.McmfSolver;
import com.logistics.model.*;
import com.logistics.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OptimizationService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final RouteRepository routeRepository;
    private final OrderRepository orderRepository;
    private final ShipmentPlanRepository shipmentPlanRepository;

    public OptimizationService(WarehouseRepository warehouseRepository,
                               InventoryRepository inventoryRepository,
                               RouteRepository routeRepository,
                               OrderRepository orderRepository,
                               ShipmentPlanRepository shipmentPlanRepository) {
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.routeRepository = routeRepository;
        this.orderRepository = orderRepository;
        this.shipmentPlanRepository = shipmentPlanRepository;
    }

    /**
     * Executes the Min-Cost Max-Flow (MCMF) algorithm for a given order,
     * determining the optimal routes and fulfilled capacity while minimizing shipping cost.
     */
    @Transactional
    public ShipmentPlan optimizeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        Product product = order.getProduct();
        int requestedQty = order.getQuantity();

        // 1. Collect all active routes and unique node names
        List<Route> activeRoutes = routeRepository.findByActiveTrue();
        List<Warehouse> allWarehouses = warehouseRepository.findAll();

        Set<String> allNodeNames = new LinkedHashSet<>();
        for (Warehouse wh : allWarehouses) {
            allNodeNames.add(wh.getName());
        }
        for (Route r : activeRoutes) {
            allNodeNames.add(r.getSourceName());
            allNodeNames.add(r.getDestinationName());
        }

        // Determine destination node name
        String destinationNode;
        if ("TRANSFER".equalsIgnoreCase(order.getOrderType())) {
            if (order.getDestinationWarehouse() == null) {
                throw new IllegalStateException("Transfer order missing destination warehouse");
            }
            destinationNode = order.getDestinationWarehouse().getName();
        } else {
            destinationNode = order.getDestinationLocation();
            if (destinationNode == null || destinationNode.trim().isEmpty()) {
                throw new IllegalStateException("Delivery order missing destination location");
            }
        }
        allNodeNames.add(destinationNode);

        // Assign numeric node IDs for FlowNetwork
        // Node 0: Super Source
        // Node 1..K: Actual network nodes (warehouses, destinations)
        // Node K+1: Super Sink
        Map<String, Integer> nodeMap = new HashMap<>();
        int currentId = 1;
        for (String nodeName : allNodeNames) {
            nodeMap.put(nodeName, currentId++);
        }

        int superSource = 0;
        int superSink = currentId;
        int totalVertices = superSink + 1;

        FlowNetwork network = new FlowNetwork(totalVertices);
        network.registerNode(superSource, "Super Source");
        network.registerNode(superSink, "Super Sink");
        for (Map.Entry<String, Integer> entry : nodeMap.entrySet()) {
            network.registerNode(entry.getValue(), entry.getKey());
        }

        // 2. Add edges from Super Source to warehouses containing the required product
        if (order.getSourceWarehouse() != null) {
            // Source warehouse explicitly specified
            Warehouse srcWh = order.getSourceWarehouse();
            Optional<Inventory> invOpt = inventoryRepository.findByWarehouseIdAndProductId(srcWh.getId(), product.getId());
            int availableStock = invOpt.map(Inventory::getAvailableQuantity).orElse(0);

            Integer srcNodeId = nodeMap.get(srcWh.getName());
            if (srcNodeId != null && availableStock > 0) {
                network.addEdge(superSource, srcNodeId, availableStock, 0.0, null,
                        "Super Source", srcWh.getName(), 0, 0);
            }
        } else {
            // Auto-sourcing: Any warehouse holding available stock can supply
            List<Inventory> inventories = inventoryRepository.findByProductId(product.getId());
            for (Inventory inv : inventories) {
                Warehouse wh = inv.getWarehouse();
                // If it's a transfer order, do not source from the destination warehouse itself
                if ("TRANSFER".equalsIgnoreCase(order.getOrderType()) &&
                        order.getDestinationWarehouse() != null &&
                        wh.getId().equals(order.getDestinationWarehouse().getId())) {
                    continue;
                }

                int availableStock = inv.getAvailableQuantity();
                Integer whNodeId = nodeMap.get(wh.getName());
                if (whNodeId != null && availableStock > 0) {
                    network.addEdge(superSource, whNodeId, availableStock, 0.0, null,
                            "Super Source", wh.getName(), 0, 0);
                }
            }
        }

        // 3. Add active transportation routes into the flow network
        for (Route route : activeRoutes) {
            Integer u = nodeMap.get(route.getSourceName());
            Integer v = nodeMap.get(route.getDestinationName());
            if (u != null && v != null) {
                network.addEdge(u, v, route.getCapacity(), route.getUnitCost(), route.getId(),
                        route.getSourceName(), route.getDestinationName(),
                        route.getDistanceKm(), route.getTravelTimeHours());
            }
        }

        // 4. Add edge from Destination to Super Sink with requested capacity
        Integer destNodeId = nodeMap.get(destinationNode);
        if (destNodeId != null) {
            network.addEdge(destNodeId, superSink, requestedQty, 0.0, null,
                    destinationNode, "Super Sink", 0, 0);
        }

        // 5. Run the Min-Cost Max-Flow solver
        McmfSolver.McmfResult result = McmfSolver.solve(network, superSource, superSink, requestedQty);

        // 6. Build ShipmentPlan with detailed hops
        List<ShipmentHop> hops = new ArrayList<>();
        for (FlowEdge edge : result.getActiveEdges()) {
            // Ignore synthetic super-source and super-sink edges
            if (edge.getFrom() == superSource || edge.getTo() == superSink) {
                continue;
            }

            double subtotal = Math.round(edge.getFlow() * edge.getCost() * 100.0) / 100.0;
            ShipmentHop hop = new ShipmentHop(
                    edge.getFromName(),
                    edge.getToName(),
                    edge.getFlow(),
                    edge.getCost(),
                    subtotal,
                    edge.getDistanceKm(),
                    edge.getTravelTimeHours(),
                    edge.getRouteId()
            );
            hops.add(hop);
        }

        // Determine feasibility and generate summary
        String feasibility;
        StringBuilder bottleneckSummary = new StringBuilder();

        if (result.isFullyFulfilled()) {
            feasibility = "OPTIMAL_FULFILLED";
            bottleneckSummary.append(String.format("Successfully fulfilled 100%% of demand (%d units) at minimum transportation cost of ₹%.2f.",
                    result.getMaxFlow(), result.getTotalCost()));
        } else if (result.getMaxFlow() > 0) {
            feasibility = "PARTIALLY_FULFILLED";
            bottleneckSummary.append(String.format("Partial fulfillment: %d of %d requested units could be routed. Remaining unfulfilled: %d units. ",
                    result.getMaxFlow(), requestedQty, result.getUnfulfilledDemand()));
            if (!result.getBottlenecks().isEmpty()) {
                bottleneckSummary.append("Constraints encountered: ").append(String.join("; ", result.getBottlenecks()));
            }
        } else {
            feasibility = "INFEASIBLE";
            bottleneckSummary.append(String.format("Unable to satisfy demand for %d units. No feasible route or inventory available between origin and destination.", requestedQty));
            if (!result.getBottlenecks().isEmpty()) {
                bottleneckSummary.append(" Reason: ").append(String.join("; ", result.getBottlenecks()));
            }
        }

        double avgUnitCost = result.getMaxFlow() > 0 ?
                Math.round((result.getTotalCost() / result.getMaxFlow()) * 100.0) / 100.0 : 0.0;

        ShipmentPlan plan = new ShipmentPlan(
                requestedQty,
                result.getMaxFlow(),
                result.getUnfulfilledDemand(),
                result.getTotalCost(),
                avgUnitCost,
                feasibility,
                bottleneckSummary.toString(),
                hops
        );

        plan = shipmentPlanRepository.save(plan);

        // Update order
        order.setShipmentPlan(plan);
        order.setStatus("OPTIMIZED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return plan;
    }
}
